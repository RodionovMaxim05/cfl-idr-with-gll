import numpy as np
import matplotlib.pyplot as plt
import os
import argparse

from utils import (
    GRAMMAR_LABELS,
    GRAMMARS,
    KOTLIN_JAR,
    run_measurements,
    analyze,
    parse_output,
    plot_over_under_diff,
    print_time_out,
)


def make_kotlin_cmd(output_dir, valueflow: bool = False):
    def kotlin_command(filename, grammar):
        cmd = [
            "java",
            "-jar",
            KOTLIN_JAR,
            "-q",
            "-o",
            output_dir,
        ]

        if valueflow:
            cmd.append("-valueflow")

        cmd.extend([filename, grammar])
        return cmd

    return kotlin_command


def run_bench_for_grammar(
    grammar: str,
    kotlin_cmd: str,
    results_file: str,
    approx_values: dict,
    over_under_diff: dict,
):
    results = {}

    with open(results_file, "a", encoding="utf-8") as f:
        for graph in GRAPHS:
            print(f"=== {graph} ===")

            raw_results = run_measurements(kotlin_cmd, graph, grammar)

            valid_times = [
                t
                for t in raw_results
                if isinstance(t, (int, float)) and not np.isnan(t)
            ]

            if valid_times:
                stats = analyze(valid_times)
                results[graph] = {
                    "mean": stats["mean"],
                    "error": stats["error"],
                }

                diff, under, over = parse_output(graph, KOTLIN_OUTPUT_DIR)
                over_under_diff[grammar].append(diff)
                approx_values[grammar][graph] = {
                    "under": under,
                    "over": over,
                    "diff": diff,
                }

                time_str = f"{stats['mean']:.3f}±{stats['error']:.3f}"
                f.write(
                    f"{grammar:8} | {os.path.basename(graph):20} | {time_str:20} | {under:10} | {over:10} | {diff:10}\n"
                )

            else:
                status = raw_results[0]
                results[graph] = {"mean": status, "error": 0.0}
                over_under_diff[grammar].append(status)
                approx_values[grammar][graph] = {
                    "under": status,
                    "over": status,
                    "diff": status,
                }
                f.write(
                    f"{grammar:8} | {os.path.basename(graph):20} | {status:20} | {status:10} | {status:10} | {status:10}\n"
                )

            f.flush()

    return results


def plot_time(grammar: str, results: dict):
    labels = [os.path.splitext(os.path.basename(g))[0] for g in GRAPHS]

    all_means = [results[g]["mean"] for g in GRAPHS]
    means = [
        m if isinstance(m, (int, float)) and not np.isnan(m) else 0.0
        for m in all_means
    ]
    errors = [results[g]["error"] for g in GRAPHS]

    x = np.arange(len(labels))

    plt.figure(figsize=(15, 7))
    plt.bar(x, means, yerr=errors, capsize=5)

    print_time_out(x, all_means)

    plt.title(f"Execution times for {GRAMMAR_LABELS[grammar]}")
    plt.xlabel("Graph")
    plt.ylabel("Execution time (s)")
    plt.xticks(x, labels)
    plt.tight_layout()

    os.makedirs("plots", exist_ok=True)
    plt.savefig(f"plots/{grammar}_comparison_of_times_{BASE_NAME}.png", dpi=200)
    plt.close()


if __name__ == "__main__":
    parser = argparse.ArgumentParser()
    parser.add_argument("input_dir")
    parser.add_argument("-valueflow", action="store_true")
    args = parser.parse_args()

    input_dir = args.input_dir.rstrip("/")
    BASE_NAME = os.path.basename(os.path.normpath(input_dir))
    valueflow = args.valueflow

    GRAPHS = sorted(
        [
            os.path.join(input_dir, f)
            for f in os.listdir(input_dir)
            if f.endswith(".dot")
        ]
    )

    KOTLIN_OUTPUT_DIR = f"{BASE_NAME}-out-gll-based"
    os.makedirs(KOTLIN_OUTPUT_DIR, exist_ok=True)

    results_file = f"plots/results_comparison_of_times_{BASE_NAME}.txt"

    with open(results_file, "w", encoding="utf-8") as f:
        f.write(
            f"{'Grammar':8} | {'Graph':20} | {'Time':20} | {'Under':10} | {'Over':10} | {'Diff':10}\n"
        )
        f.write("-" * 93 + "\n")

    kotlin_cmd = make_kotlin_cmd(KOTLIN_OUTPUT_DIR, valueflow)

    over_under_diff = {g: [] for g in GRAMMARS}
    approx_values = {g: {} for g in GRAMMARS}
    results_dict = {}

    for grammar in GRAMMARS:
        print("\n" + "=" * 44)
        print(
            f"\tGrammar: {grammar} ({GRAMMAR_LABELS[grammar]}), Value-flow: {valueflow}"
        )
        print("=" * 44 + "\n")

        res = run_bench_for_grammar(
            grammar, kotlin_cmd, results_file, approx_values, over_under_diff
        )
        results_dict[grammar] = res
        plot_time(grammar, res)

        with open(results_file, "a", encoding="utf-8") as f:
            f.write("\n")

    labels = [os.path.splitext(os.path.basename(g))[0] for g in GRAPHS]
    plot_over_under_diff(
        over_under_diff, labels, f"over_under_diff_all_grammars_{BASE_NAME}"
    )
