import subprocess
import time
import numpy as np
from scipy import stats
import matplotlib.pyplot as plt
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

GRAPHS = sorted([f"taint/{f}" for f in os.listdir("taint") if f.endswith(".dot")])

REPEATS = 8

GO_CMD = lambda filename, grammar: ["./comparable_impl/algo_go", filename, grammar]

KOTLIN_JAR = "../build/libs/cfl-idr-with-gll-all.jar"
KOTLIN_CMD = lambda filename, grammar: [
    "java",
    "-jar",
    KOTLIN_JAR,
    "-q",
    "-o",
    "taint-out-gll-based",
    filename,
    grammar,
]


def measure_time(cmd):
    start = time.perf_counter()
    subprocess.run(cmd, shell=False, stdout=subprocess.PIPE, stderr=subprocess.PIPE)
    return time.perf_counter() - start


def analyze(times):
    t = np.array(times)

    p1 = stats.normaltest(t).pvalue
    p2 = stats.shapiro(t).pvalue
    normal = (p1 > 0.05) or (p2 > 0.05)

    mean_raw = np.mean(t)
    std_raw = np.std(t, ddof=1)

    ci_raw = stats.t.ppf(0.975, df=len(t) - 1) * stats.sem(t)
    error = ci_raw

    return {
        "mean": mean_raw,
        "error": error,
        "mean_raw": mean_raw,
        "std_raw": std_raw,
        "ci_raw": ci_raw,
        "normal": normal,
    }


def run_bench_for_grammar(grammar):
    results = {}

    for graph in GRAPHS:
        print(f"=== {grammar} | {graph} ===")

        # --- Go ---
        go_times = [measure_time(GO_CMD(graph, grammar)) for _ in range(REPEATS)]
        go_res = analyze(go_times)

        # --- Kotlin ---
        kt_times = [measure_time(KOTLIN_CMD(graph, grammar)) for _ in range(REPEATS)]
        kt_res = analyze(kt_times)

        results[graph] = {"go": go_res, "kotlin": kt_res}

    return results


def plot_for_grammar(grammar, results):
    labels = [os.path.splitext(os.path.basename(g))[0] for g in GRAPHS]

    go_means = [results[g]["go"]["mean"] for g in GRAPHS]
    kt_means = [results[g]["kotlin"]["mean"] for g in GRAPHS]

    go_err = [results[g]["go"]["error"] for g in GRAPHS]
    kt_err = [results[g]["kotlin"]["error"] for g in GRAPHS]

    x = np.arange(len(labels))
    width = 0.35

    plt.figure(figsize=(15, 7))
    plt.bar(
        x - width / 2,
        go_means,
        width,
        label="Original Go impl",
        yerr=go_err,
        capsize=5,
    )
    plt.bar(
        x + width / 2,
        kt_means,
        width,
        label="GLL-based impl",
        yerr=kt_err,
        capsize=5,
    )

    plt.ylabel("Execution Time (s)")
    plt.xlabel("Graph")
    plt.title(f"Comparison of algorithms for grammar {GRAMMAR_LABELS[grammar]}")
    plt.xticks(x, labels)
    plt.legend()
    plt.tight_layout()

    os.makedirs("plots", exist_ok=True)
    plt.savefig(f"plots/{grammar}.png", dpi=200)
    plt.close()


if __name__ == "__main__":
    for grammar in GRAMMARS:
        print(f"\n=================================")
        print(f"\tGrammar: {grammar} ({GRAMMAR_LABELS[grammar]})")
        print(f"=================================\n")

        res = run_bench_for_grammar(grammar)
        plot_for_grammar(grammar, res)

    print("\nDone! The graphs are saved in the plots/ folder.")
