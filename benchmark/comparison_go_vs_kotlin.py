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
    print_time_out,
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
        go_valid_times = [t for t in go_times if not np.isnan(t)]
        if go_valid_times:
            stats = analyze(go_valid_times)
            go_result = {"mean": stats["mean"], "error": stats["error"]}
        else:
            go_result = {"mean": np.nan, "error": 0.0}

        # --- Kotlin ---
        kt_times = [measure_time(KOTLIN_CMD(graph, grammar)) for _ in range(REPEATS)]
        kt_valid_times = [t for t in kt_times if not np.isnan(t)]
        if kt_valid_times:
            stats = analyze(kt_valid_times)
            kt_result = {"mean": stats["mean"], "error": stats["error"]}
        else:
            kt_result = {"mean": np.nan, "error": 0.0}

        results[graph] = {"go": go_result, "kotlin": kt_result}

    return results


def plot_for_grammar(grammar: str, results: dict):
    labels = [os.path.splitext(os.path.basename(g))[0] for g in GRAPHS]

    go_all_means = [results[g]["go"]["mean"] for g in GRAPHS]
    go_means = [m if not np.isnan(m) else 0.0 for m in go_all_means]
    kt_all_means = [results[g]["kotlin"]["mean"] for g in GRAPHS]
    kt_means = [m if not np.isnan(m) else 0.0 for m in kt_all_means]

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

    print_time_out(x - width / 2, go_all_means)
    print_time_out(x + width / 2, kt_all_means)

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
            if not np.isnan(res[graph]["kotlin"]["mean"]):
                value = parse_kotlin_output(graph, grammar, KOTLIN_OUTPUT_DIR)
                over_under_diff[grammar].append(value)
            else:
                over_under_diff[grammar].append(np.nan)

    labels = [os.path.splitext(os.path.basename(g))[0] for g in GRAPHS]
    plot_over_under_diff(
        over_under_diff, labels, "over_under_diff_all_grammars_go_vs_kotlin"
    )

    print("\nDone! comprasion_go_vs_kotlin finished.")
