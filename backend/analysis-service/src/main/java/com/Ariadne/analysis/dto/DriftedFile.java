package com.Ariadne.analysis.dto;

public record DriftedFile(String path, int prevFanIn, int newFanIn, int prevFanOut, int newFanOut) {}