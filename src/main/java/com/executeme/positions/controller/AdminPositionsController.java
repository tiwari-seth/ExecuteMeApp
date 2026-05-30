package com.executeme.positions.controller;

import com.executeme.broker.model.BrokerPosition;
import com.executeme.positions.service.PositionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/positions")
@Tag(name = "Positions")
@SecurityRequirement(name = "basicAuth")
public class AdminPositionsController {

    private final PositionService positionService;

    public AdminPositionsController(PositionService positionService) {
        this.positionService = positionService;
    }

    @GetMapping
    @Operation(
            summary = "List broker positions",
            description = "Returns the current positions visible through active Angel One broker sessions."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Positions returned",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = BrokerPosition.class)))),
            @ApiResponse(responseCode = "401", description = "Admin authentication is required", content = @Content),
            @ApiResponse(responseCode = "403", description = "Admin APIs are disabled", content = @Content)
    })
    public List<BrokerPosition> positions() {
        return positionService.allAngelPositions();
    }
}
