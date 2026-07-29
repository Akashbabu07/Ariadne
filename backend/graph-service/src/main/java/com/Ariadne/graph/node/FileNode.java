package com.Ariadne.graph.node;

import lombok.*;
import org.springframework.data.neo4j.core.schema.GeneratedValue;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;

@Node("File")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class FileNode {

    @Id
    @GeneratedValue
    private Long id;

    private String path;
    private String language;
}