
package com.Ariadne.graph.repository;

public interface FileMetricsProjection {
    String getPath();
    String getLanguage();
    Long getFanIn();
    Long getFanOut();
}