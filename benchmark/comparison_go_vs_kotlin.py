import numpy as np
import matplotlib.pyplot as plt
import os
import sys

from utils import (
    GRAMMAR_LABELS,
    GRAMMARS,
    KOTLIN_JAR,
    run_measurements,
    analyze,
    parse_kotlin_output,
    plot_over_under_diff,
    print_time_out,
)

INPUT_DIR = sys.argv[1] if len(sys.argv) > 1 else "taint"

GRAPHS = sorted(
    [f"{INPUT_DIR}/{f}" for f in os.listdir(INPUT_DIR) if f.endswith(".dot")]
)

GO_CMD = lambda filename, grammar: ["./comparable_impl/algo_go", filename, grammar]

KOTLIN_OUTPUT_DIR = f"{INPUT_DIR}-out-gll-based"
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


def process_results(raw_times: list) -> dict:
    valid_times = [
        t for t in raw_times if isinstance(t, (int, float)) and not np.isnan(t)
    ]

    if valid_times:
        stats = analyze(valid_times)
        return {"mean": stats["mean"], "error": stats["error"]}
    else:
        return {"mean": raw_times[0], "error": 0.0}


def run_bench_for_grammar(
    grammar: str,
    results_file: str,
    approx_values: dict,
    over_under_diff: dict,
    is_first_grammar: bool,
):
    results = {}

    go_file = results_file.replace(".txt", "_go.txt")
    kotlin_file = results_file.replace(".txt", "_kotlin.txt")

    if is_first_grammar:
        os.makedirs(KOTLIN_OUTPUT_DIR, exist_ok=True)
        open(go_file, "w", encoding="utf-8").close()
        open(kotlin_file, "w", encoding="utf-8").close()

    f_go = open(go_file, "a", encoding="utf-8")
    f_kotlin = open(kotlin_file, "a", encoding="utf-8")

    if os.path.getsize(go_file) == 0:
        f_go.write(f"{'Grammar':8} | {'Graph':20} | {'Time':20}\n")
        f_go.write("-" * 48 + "\n")

    if os.path.getsize(kotlin_file) == 0:
        f_kotlin.write(
            f"{'Grammar':8} | {'Graph':20} | {'Time':20} | {'Under':10} | {'Over':10} | {'Diff':10}\n"
        )
        f_kotlin.write("-" * 93 + "\n")

    for graph in GRAPHS:
        print(f"=== {graph} ===")

        # --- Go ---
        go_raw = run_measurements(GO_CMD, graph, grammar)
        go_result = process_results(go_raw)
        results[graph] = {"go": go_result}

        graph_name = os.path.splitext(os.path.basename(graph))[0]
        if isinstance(go_result["mean"], (int, float)):
            time_str = f"{go_result['mean']:.3f}±{go_result['error']:.3f}"
            f_go.write(
                f"{GRAMMAR_LABELS[grammar]:8} | {graph_name:20} | {time_str:20}\n"
            )
        else:
            f_go.write(
                f"{GRAMMAR_LABELS[grammar]:8} | {graph_name:20} | {go_result['mean']:20}\n"
            )
        f_go.flush()

        # --- Kotlin ---
        kt_raw = run_measurements(KOTLIN_CMD, graph, grammar)
        kt_result = process_results(kt_raw)
        results[graph]["kotlin"] = kt_result

        if isinstance(kt_result["mean"], (int, float)) and not np.isnan(
            kt_result["mean"]
        ):
            diff, under, over = parse_kotlin_output(graph, KOTLIN_OUTPUT_DIR)

            over_under_diff[grammar].append(diff)
            approx_values[grammar][graph] = {
                "under": under,
                "over": over,
            }

            time_str = f"{kt_result['mean']:.3f}±{kt_result['error']:.3f}"
            f_kotlin.write(
                f"{GRAMMAR_LABELS[grammar]:8} | {graph_name:20} | {time_str:20} | {under:10} | {over:10} | {diff:10}\n"
            )
        else:
            status = kt_result["mean"]
            over_under_diff[grammar].append(status)
            approx_values[grammar][graph] = {"under": status, "over": status}

            f_kotlin.write(
                f"{GRAMMAR_LABELS[grammar]:8} | {graph_name:20} | {status:20} | {status:10} | {status:10} | {status:10}\n"
            )

        f_kotlin.flush()

    f_go.close()
    f_kotlin.close()

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

    plt.title(f"Comparison ({INPUT_DIR}) for grammar {GRAMMAR_LABELS[grammar]}")
    plt.xlabel("Graph")
    plt.ylabel("Execution time (s)")
    plt.xticks(x, labels)
    plt.legend()
    plt.tight_layout()

    os.makedirs("plots", exist_ok=True)
    plt.savefig(f"plots/{grammar}_go_vs_kotlin_{INPUT_DIR}.png", dpi=200)
    plt.close()


if __name__ == "__main__":
    over_under_diff = {g: [] for g in GRAMMARS}
    approx_values = {g: {} for g in GRAMMARS}
    results_file = f"plots/benchmark_results_{INPUT_DIR}.txt"
    is_first_run = True

    for grammar in GRAMMARS:
        print(f"\n" + "=" * 44)
        print(f"\tGrammar: {grammar} ({GRAMMAR_LABELS[grammar]})")
        print("=" * 44 + "\n")

        res = run_bench_for_grammar(
            grammar, results_file, approx_values, over_under_diff, is_first_run
        )

        if is_first_run:
            is_first_run = False

        plot_for_grammar(grammar, res)

    labels = [os.path.splitext(os.path.basename(g))[0] for g in GRAPHS]
    plot_over_under_diff(
        over_under_diff,
        labels,
        f"over_under_diff_all_grammars_go_vs_kotlin_{INPUT_DIR}",
    )
