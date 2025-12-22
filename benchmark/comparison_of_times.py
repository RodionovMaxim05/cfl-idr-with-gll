import numpy as np
import matplotlib.pyplot as plt
import os
import argparse

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


def run_bench_for_grammar(grammar: str, graphs: list, kotlin_cmd):
    results = {}

    for graph in graphs:
        print(f"=== {graph} ===")

        times = [measure_time(kotlin_cmd(graph, grammar)) for _ in range(REPEATS)]
        results[graph] = analyze(times)

    return results


def plot_time(grammar: str, results: dict, graphs: list, input_dir: str):
    labels = [os.path.splitext(os.path.basename(g))[0] for g in graphs]

    means = [results[g]["mean"] for g in graphs]
    errors = [results[g]["error"] for g in graphs]

    x = np.arange(len(labels))

    plt.figure(figsize=(15, 7))
    plt.bar(x, means, yerr=errors, capsize=5)

    plt.title(f"Execution times for {GRAMMAR_LABELS[grammar]}")
    plt.xlabel("Graph")
    plt.ylabel("Execution time (s)")
    plt.xticks(x, labels)
    plt.tight_layout()

    os.makedirs("plots", exist_ok=True)
    plt.savefig(f"plots/{grammar}_comparison_of_times_{input_dir}.png", dpi=200)
    plt.close()


if __name__ == "__main__":
    parser = argparse.ArgumentParser(
        description="Run benchmark on graphs from a given directory"
    )
    parser.add_argument(
        "input_dir",
        help="Directory with .dot graph files",
    )
    parser.add_argument(
        "-valueflow",
        action="store_true",
        help="Enable value-flow mode for the analysis",
    )
    args = parser.parse_args()

    input_dir = args.input_dir.rstrip("/")
    valueflow = args.valueflow

    GRAPHS = sorted(
        [
            os.path.join(input_dir, f)
            for f in os.listdir(input_dir)
            if f.endswith(".dot")
        ]
    )

    KOTLIN_OUTPUT_DIR = f"{input_dir}-out-gll-based"
    os.makedirs(KOTLIN_OUTPUT_DIR, exist_ok=True)

    kotlin_cmd = make_kotlin_cmd(KOTLIN_OUTPUT_DIR, valueflow)

    over_under_diff = {g: [] for g in GRAMMARS}

    for grammar in GRAMMARS:
        print(f"\n============================================")
        print(
            f"\tGrammar: {grammar} ({GRAMMAR_LABELS[grammar]}), Value-flow: {valueflow}"
        )
        print(f"============================================\n")

        res = run_bench_for_grammar(grammar, GRAPHS, kotlin_cmd)
        plot_time(grammar, res, GRAPHS, input_dir)

        for graph in GRAPHS:
            over_under_diff[grammar].append(
                parse_kotlin_output(graph, grammar, KOTLIN_OUTPUT_DIR)
            )

    labels = [os.path.splitext(os.path.basename(g))[0] for g in GRAPHS]
    plot_over_under_diff(
        over_under_diff, labels, f"over_under_diff_all_grammars_{input_dir}"
    )

    print("\nDone! benchmark finished.")
