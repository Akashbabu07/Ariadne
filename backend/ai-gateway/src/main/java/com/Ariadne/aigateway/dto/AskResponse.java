
package com.Ariadne.aigateway.dto;

import java.util.List;
import java.util.UUID;

public record AskResponse(UUID repositoryId, String mode, String answer, List<String> sources) {}