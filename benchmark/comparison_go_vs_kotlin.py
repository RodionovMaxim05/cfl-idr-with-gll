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
    save_benchmark_results,
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


def process_results(raw_times):
    valid_times = [
        t for t in raw_times if isinstance(t, (int, float)) and not np.isnan(t)
    ]

    if valid_times:
        stats = analyze(valid_times)
        return {"mean": stats["mean"], "error": stats["error"]}
    else:
        if "SOF" in raw_times:
            status = "SOF"
        elif "OOM" in raw_times:
            status = "OOM"
        elif "T/O" in raw_times:
            status = "T/O"
        else:
            status = np.nan
        return {"mean": status, "error": 0.0}


def run_bench_for_grammar(grammar: str):
    results = {}

    for graph in GRAPHS:
        print(f"=== {graph} ===")

        # --- Go ---
        go_raw = [measure_time(GO_CMD(graph, grammar)) for _ in range(REPEATS)]
        results[graph] = {"go": process_results(go_raw)}

        # --- Kotlin ---
        kt_raw = [measure_time(KOTLIN_CMD(graph, grammar)) for _ in range(REPEATS)]
        results[graph]["kotlin"] = process_results(kt_raw)

    return results


def plot_for_grammar(grammar: str, results: dict):
    labels = [os.path.splitext(os.path.basename(g))[0] for g in GRAPHS]

    go_labels = [results[g]["go"]["mean"] for g in GRAPHS]
    kt_labels = [results[g]["kotlin"]["mean"] for g in GRAPHS]

    go_means = [m if isinstance(m, (int, float)) else 0.0 for m in go_labels]
    kt_means = [m if isinstance(m, (int, float)) else 0.0 for m in kt_labels]

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

    print_time_out(x - width / 2, go_labels)
    print_time_out(x + width / 2, kt_labels)

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
    approx_values = {g: {} for g in GRAMMARS}
    results_dict_go = {}
    results_dict_kotlin = {}

    for grammar in GRAMMARS:
        print(f"\n" + "=" * 44)
        print(f"\tGrammar: {grammar} ({GRAMMAR_LABELS[grammar]})")
        print("=" * 44 + "\n")

        res = run_bench_for_grammar(grammar)
        plot_for_grammar(grammar, res)

        go_results = {}
        kotlin_results = {}

        for graph in GRAPHS:
            go_results[graph] = res[graph]["go"]
            kotlin_results[graph] = res[graph]["kotlin"]

            kt_res = res[graph]["kotlin"]["mean"]
            if isinstance(kt_res, (int, float)) and not np.isnan(kt_res):
                diff, under, over = parse_kotlin_output(graph, KOTLIN_OUTPUT_DIR)
                over_under_diff[grammar].append(diff)
                approx_values[grammar][graph] = {"under": under, "over": over}
            else:
                over_under_diff[grammar].append(kt_res)
                approx_values[grammar][graph] = {"under": kt_res, "over": kt_res}

        results_dict_go[grammar] = go_results
        results_dict_kotlin[grammar] = kotlin_results

    save_benchmark_results(
        results_dict_go,
        {},
        GRAPHS,
        "go_benchmark_results.txt",
        approx_values=None,
    )

    save_benchmark_results(
        results_dict_kotlin,
        over_under_diff,
        GRAPHS,
        "plots/kotlin_benchmark_results.txt",
        approx_values,
    )

    labels = [os.path.splitext(os.path.basename(g))[0] for g in GRAPHS]
    plot_over_under_diff(
        over_under_diff, labels, "over_under_diff_all_grammars_go_vs_kotlin"
    )

    print("\nDone! Comparison benchmark finished.")
