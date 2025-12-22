import numpy as np
import matplotlib.pyplot as plt
import os

from utils import (
    GRAMMAR_LABELS,
    GRAMMARS,
    KOTLIN_JAR,
    measure_time,
    analyze,
    parse_kotlin_output,
    plot_over_under_diff,
)

REPEATS = 1

GRAPHS = sorted([f"taint/{f}" for f in os.listdir("taint") if f.endswith(".dot")])

GO_CMD = lambda filename, grammar: ["./comparable_impl/algo_go", filename, grammar]

KOTLIN_OUTPUT_DIR = "taint-out-gll-based"
KOTLIN_CMD = lambda filename, grammar: [
    "java",
    "-jar",
    KOTLIN_JAR,
    "-q",
    "-o",
    KOTLIN_OUTPUT_DIR,
    filename,
    grammar,
]


def run_bench_for_grammar(grammar: str):
    results = {}

    for graph in GRAPHS:
        print(f"=== {graph} ===")

        # --- Go ---
        go_times = [measure_time(GO_CMD(graph, grammar)) for _ in range(REPEATS)]
        go_res = analyze(go_times)

        # --- Kotlin ---
        kt_times = [measure_time(KOTLIN_CMD(graph, grammar)) for _ in range(REPEATS)]
        kt_res = analyze(kt_times)

        results[graph] = {"go": go_res, "kotlin": kt_res}

    return results


def plot_for_grammar(grammar: str, results: dict):
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
        yerr=go_err,
        label="Original Go impl",
        capsize=5,
    )
    plt.bar(
        x + width / 2,
        kt_means,
        width,
        yerr=kt_err,
        label="GLL-based impl",
        capsize=5,
    )

    plt.title(f"Comparison of algorithms for grammar {GRAMMAR_LABELS[grammar]}")
    plt.xlabel("Graph")
    plt.ylabel("Execution time (s)")
    plt.xticks(x, labels)
    plt.legend()
    plt.tight_layout()

    os.makedirs("plots", exist_ok=True)
    plt.savefig(f"plots/{grammar}_go_vs_kotlin.png", dpi=200)
    plt.close()


if __name__ == "__main__":
    over_under_diff = {g: [] for g in GRAMMARS}

    for grammar in GRAMMARS:
        print(f"\n=================================")
        print(f"\tGrammar: {grammar} ({GRAMMAR_LABELS[grammar]})")
        print(f"=================================\n")

        res = run_bench_for_grammar(grammar)
        plot_for_grammar(grammar, res)

        for graph in GRAPHS:
            over_under_diff[grammar].append(
                parse_kotlin_output(graph, grammar, KOTLIN_OUTPUT_DIR)
            )

    labels = [os.path.splitext(os.path.basename(g))[0] for g in GRAPHS]
    plot_over_under_diff(
        over_under_diff, labels, "over_under_diff_all_grammars_go_vs_kotlin"
    )

    print("\nDone! comprasion_go_vs_kotlin finished.")
