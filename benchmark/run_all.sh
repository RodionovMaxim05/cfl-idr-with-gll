#!/usr/bin/env bash

echo "Running comparison_go_vs_kotlin benchmark..."
python3 comparison_go_vs_kotlin.py

echo "Running comparison_of_times benchmark for valueflow/..."
python3 comparison_of_times.py -valueflow valueflow

echo "Running comparison_of_times benchmark for taint-additional/..."
python3 comparison_of_times.py taint-additional

echo "All benchmarks finished."
