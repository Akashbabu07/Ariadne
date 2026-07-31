package com.Ariadne.graph.node;

import lombok.*;
import org.springframework.data.neo4j.core.schema.GeneratedValue;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Relationship;

import java.util.ArrayList;
import java.util.List;

@Node("File")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class FileNode {

    @Id
    @GeneratedValue
    private Long id;

    private String path;
    private String language;

    @Relationship(type = "DEPENDS_ON", direction = Relationship.Direction.OUTGOING)
    @Builder.Default
    private List<FileNode> dependsOn = new ArrayList<>();
}