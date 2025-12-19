import subprocess
import time
import numpy as np
import matplotlib.pyplot as plt
from scipy import stats
import os

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


def measure_time(cmd: list) -> float:
    start = time.perf_counter()
    subprocess.run(
        cmd,
        shell=False,
        stdout=subprocess.PIPE,
        stderr=subprocess.PIPE,
    )
    return time.perf_counter() - start


def analyze(times: list[float]) -> dict:
    t = np.array(times)

    normal_test = stats.normaltest(t).pvalue
    shapiro_test = stats.shapiro(t).pvalue
    is_normal = (normal_test > 0.05) or (shapiro_test > 0.05)

    mean_time = np.mean(t)
    conf_interval = stats.t.ppf(0.975, df=len(t) - 1) * stats.sem(t)

    return {
        "mean": mean_time,
        "error": conf_interval,
        "is_normal": is_normal,
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

    all_values = []
    for grammar in GRAMMARS:
        all_values.extend(over_under_diff[grammar])

    max_value = max(all_values)
    ax.set_xlim(0, max_value * 1.1)

    for i, grammar in enumerate(GRAMMARS):
        bars = ax.barh(
            y + i * height,
            over_under_diff[grammar],
            height,
            label=GRAMMAR_LABELS[grammar],
        )
        ax.bar_label(
            bars,
            labels=[f"{int(h)}" for h in over_under_diff[grammar]],
            padding=3,
            fontsize=9,
            label_type="edge",
        )

    ax.set_title("|$R_{over} - R_{under}$| for all grammars")
    ax.set_ylabel("Graph")
    ax.set_xlabel("|$R_{over} - R_{under}$|")
    ax.set_yticks(y + height * (len(GRAMMARS) - 1) / 2)
    ax.set_yticklabels(labels)
    ax.legend(ncol=2)

    fig.tight_layout()
    fig.savefig(f"plots/{filename}.png", dpi=200)
    plt.close(fig)
