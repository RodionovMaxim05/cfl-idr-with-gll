import subprocess
import time
import numpy as np
import matplotlib.pyplot as plt
from scipy import stats
import os
import subprocess
import sys
from typing import Any

TIMEOUT_SECONDS = 7200  # 2 hours

GRAMMAR_LABELS = {
    "parity": "PAR",
    "parity2": "PAR2",
    "se": "PAR2E",
    "project": "PARUnl",
    "exclude": "PARErase",
    "all": "COM",
    "on-demand": "COMD",
}

GRAMMARS = list(GRAMMAR_LABELS.keys())

KOTLIN_JAR = "../build/libs/cfl-idr-with-gll-all.jar"


def measure_time(cmd):
    try:
        start = time.perf_counter()
        subprocess.run(
            cmd,
            shell=False,
            stdout=subprocess.PIPE,
            stderr=subprocess.PIPE,
            timeout=TIMEOUT_SECONDS,
        )
        return time.perf_counter() - start
    except subprocess.TimeoutExpired:
        print("Command timed out", file=sys.stderr)
        return np.nan
    except Exception as e:
        print(f"Error running command: {e}", file=sys.stderr)
        return np.nan


def analyze(times: list[float]) -> dict:
    t = np.array(times)

    mean_time = np.mean(t)
    conf_interval = stats.t.ppf(0.975, df=len(t) - 1) * stats.sem(t)

    return {
        "mean": mean_time,
        "error": conf_interval,
    }


def parse_kotlin_output(graph: str, grammar: str, dirname: str) -> int:
    name = os.path.splitext(os.path.basename(graph))[0]
    path = os.path.join(f"{dirname}", f"{name}.out")

    under = None
    over = None
    on_demand = None

    with open(path, "r") as f:
        for line in f:
            if line.startswith("Under approximation paths:"):
                under = int(line.split(":")[1].strip())
            elif line.startswith("Over approximation paths"):
                over = int(line.split(":")[1].strip())
            elif line.startswith("On-Demand paths:"):
                on_demand = int(line.split(":")[1].strip())

    if under is None:
        raise RuntimeError(f"Missing under-approx in {path}")

    if grammar == "on-demand":
        return abs(on_demand - under)
    else:
        return abs(over - under)


def plot_over_under_diff(over_under_diff: dict, labels: list, filename: str):
    y = np.arange(len(labels))
    height = 0.8 / len(GRAMMARS)

    fig, ax = plt.subplots(figsize=(10, 12))

    all_finite_values = []
    for grammar in GRAMMARS:
        arr = np.array(over_under_diff[grammar])
        all_finite_values.extend(arr[np.isfinite(arr)])

    max_value = max(all_finite_values) if all_finite_values else 1.0
    ax.set_xlim(0, max_value * 1.1)

    for i, grammar in enumerate(GRAMMARS):
        values = np.array(over_under_diff[grammar])
        plot_values = np.where(np.isfinite(values), values, 0)
        bars = ax.barh(
            y + i * height,
            plot_values,
            height,
            label=GRAMMAR_LABELS[grammar],
        )

        labels_for_bars = []
        for v in values:
            if np.isfinite(v):
                labels_for_bars.append(f"{int(v)}")
            else:
                labels_for_bars.append("T/O")

        ax.bar_label(
            bars,
            labels=labels_for_bars,
            padding=3,
            fontsize=9,
            label_type="edge",
        )

    ax.set_title(r"|$R_{over} - R_{under}$| for all grammars")
    ax.set_ylabel("Graph")
    ax.set_xlabel(r"|$R_{over} - R_{under}$|")
    ax.set_yticks(y + height * (len(GRAMMARS) - 1) / 2)
    ax.set_yticklabels(labels)
    ax.legend(ncol=2)

    fig.tight_layout()
    fig.savefig(f"plots/{filename}.png", dpi=200)
    plt.close(fig)


def print_time_out(
    x: np.ndarray[tuple[int], np.dtype[np.signedinteger[Any]]], all_means: list
):
    for i, mean in enumerate(all_means):
        if np.isnan(mean):
            plt.text(
                x[i],
                0.0,
                "T/O",
                ha="center",
                va="bottom",
                fontsize=10,
                color="red",
                fontweight="bold",
                bbox=dict(
                    boxstyle="round,pad=0.2", facecolor="white", edgecolor="red"
                ),
            )
