#!/usr/bin/env bash
set -e

echo "Running comparison_go_vs_kotlin benchmark..."
python3 comprasion_go_vs_kotlin.py

echo "Running comparison_of_times benchmark for taint-additional/ ..."
python3 comparison_of_times.py taint-additional

echo "All benchmarks finished."
