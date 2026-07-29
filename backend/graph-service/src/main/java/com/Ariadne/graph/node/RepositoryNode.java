package com.Ariadne.graph.node;

import lombok.*;
import org.springframework.data.neo4j.core.schema.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Node("Repository")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class RepositoryNode {

    @Id
    private String id;

    private String gitUrl;

    @Relationship(type = "CONTAINS", direction = Relationship.Direction.OUTGOING)
    @Builder.Default
    private List<FileNode> files = new ArrayList<>();
}