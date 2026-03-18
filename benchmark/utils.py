import subprocess
import time
import os
import sys
import numpy as np
import matplotlib.pyplot as plt
from scipy import stats
from typing import Any, Union, List

TIMEOUT_SECONDS = 7200  # 2 hours
REPEATS = 5

GRAMMAR_LABELS = {
    "parity": "PAR",
    "parity2": "PAR2",
    "se": "PAR2E",
    "project": "PARUnl",
    "exclude": "PARErase",
    "parityD": "PARD",
    "all": "COM",
    "on-demand": "COMD",
}

GRAMMARS = list(GRAMMAR_LABELS.keys())

KOTLIN_JAR = "../build/libs/cfl-idr-with-gll-all.jar"


def run_measurements(
    cmd_func: List[str], filename: str, grammar: str
) -> List[Union[float, str]]:
    results = []

    for i in range(REPEATS):
        res = measure_time_and_memory(cmd_func(filename, grammar))
        print(res)
        results.append(res)
        if i == 0 and not isinstance(res, tuple):
            break

    return results


def measure_time_and_memory(cmd: List[str]) -> Union[tuple, str]:
    try:
        flags = []
        full_cmd = ["/usr/bin/time", "-f", "%M"] + [cmd[0]] + flags + cmd[1:]

        start = time.perf_counter()
        result = subprocess.run(
            full_cmd,
            shell=False,
            stdout=subprocess.PIPE,
            stderr=subprocess.PIPE,
            timeout=TIMEOUT_SECONDS,
            text=True,
        )

        elapsed = time.perf_counter() - start

        if "java.lang.OutOfMemoryError" in result.stderr:
            print(f"OOM detected for command: {' '.join(cmd)}", file=sys.stderr)
            return "OOM"

        if "java.lang.StackOverflowError" in result.stderr:
            print(f"StackOverflow detected: {' '.join(cmd)}", file=sys.stderr)
            return "SOF"

        if result.returncode != 0:
            print(
                f"Command failed with code {result.returncode}: {result.stderr}",
                file=sys.stderr,
            )
            return np.nan

        peak_rss_kb = int(result.stderr.strip().splitlines()[-1])
        peak_rss_mb = peak_rss_kb / 1024

        return elapsed, peak_rss_mb

    except subprocess.TimeoutExpired:
        print("Command timed out", file=sys.stderr)
        return "T/O"
    except Exception as e:
        print(f"Error running command: {e}", file=sys.stderr)
        return np.nan


def analyze(raw_results: list) -> dict:
    valid = [r for r in raw_results if isinstance(r, tuple)]

    times = np.array([r[0] for r in valid])
    rss = np.array([r[1] for r in valid if not np.isnan(r[1])])

    mean_time = np.mean(times)
    conf_interval = (
        stats.t.ppf(0.975, df=len(times) - 1) * stats.sem(times)
        if len(times) > 1
        else 0.0
    )

    mean_rss = np.mean(rss) if len(rss) > 0 else float("nan")
    rss_error = (
        stats.t.ppf(0.975, df=len(rss) - 1) * stats.sem(rss) if len(rss) > 1 else 0.0
    )

    return {
        "mean": mean_time,
        "error": conf_interval,
        "peak_rss_mb": mean_rss,
        "rss_error": rss_error,
    }


def parse_output(graph: str, dirname: str) -> tuple:
    name = os.path.splitext(os.path.basename(graph))[0]
    path = os.path.join(f"{dirname}", f"{name}.out")

    under = None
    over = None

    with open(path, "r") as f:
        for line in f:
            if line.startswith("Underapproximation:") or line.startswith(
                "Under approximation paths:"
            ):
                under = int(line.split(":")[1].strip())
            elif line.startswith("Refinement loop") or line.startswith(
                "Over approximation paths"
            ):
                over = int(line.split(":")[1].strip())
            elif line.startswith("On-Demand:") or line.startswith(
                "On-Demand paths:"
            ):
                over = int(line.split(":")[1].strip())

    if under is None:
        raise RuntimeError(f"Missing under-approx in {path}")

    diff = abs(over - under) if over is not None else 0

    return diff, under, over


def plot_over_under_diff(over_under_diff: dict, labels: list, filename: str):
    y = np.arange(len(labels))
    n_grammars = len(GRAMMARS)
    height = 0.8 / n_grammars

    fig, ax = plt.subplots(figsize=(10, 12))

    all_numeric_values = []
    for grammar in GRAMMARS:
        for val in over_under_diff[grammar]:
            if isinstance(val, (int, float)) and np.isfinite(val):
                all_numeric_values.append(val)

    max_value = max(all_numeric_values) if all_numeric_values else 1.0
    ax.set_xlim(0, max_value * 1.1)

    for i, grammar in enumerate(GRAMMARS):
        raw_values = over_under_diff[grammar]

        plot_values = [
            v if isinstance(v, (int, float)) and np.isfinite(v) else 0
            for v in raw_values
        ]

        bars = ax.barh(
            y + (n_grammars - 1 - i) * height,
            plot_values,
            height,
            label=GRAMMAR_LABELS[grammar],
        )

        labels_for_bars = []
        for v in raw_values:
            if isinstance(v, (int, float)) and np.isfinite(v):
                labels_for_bars.append(f"{int(v)}")
            elif v == "OOM":
                labels_for_bars.append("OOM")
            elif v == "SOF":
                labels_for_bars.append("SOF")
            elif v == "T/O":
                labels_for_bars.append("T/O")
            else:
                labels_for_bars.append("ERR")

        ax.bar_label(
            bars, labels=labels_for_bars, padding=3, fontsize=7, label_type="edge"
        )

    ax.set_title(r"|$R_{over} - R_{under}$| for all grammars")
    ax.set_ylabel("Graph")
    ax.set_xlabel(r"|$R_{over} - R_{under}$|")
    ax.set_yticks(y + height * (n_grammars - 1) / 2)
    ax.set_yticklabels(labels)
    ax.legend(ncol=2)

    fig.tight_layout()
    fig.savefig(f"plots/{filename}.png", dpi=200)
    plt.close(fig)


def print_time_out(
    x: np.ndarray[tuple[int], np.dtype[np.signedinteger[Any]]], all_means: list
):
    for i, mean in enumerate(all_means):
        label = None
        color = "red"

        if mean == "T/O" or (isinstance(mean, float) and np.isnan(mean)):
            label = "T/O"
        elif mean == "OOM":
            label = "OOM"
            color = "purple"
        elif mean == "SOF":
            label = "SOF"
            color = "orange"

        if label:
            plt.text(
                x[i],
                0.0,
                label,
                ha="center",
                va="bottom",
                fontsize=10,
                color=color,
                fontweight="bold",
                bbox=dict(
                    boxstyle="round,pad=0.2", facecolor="white", edgecolor=color
                ),
            )
