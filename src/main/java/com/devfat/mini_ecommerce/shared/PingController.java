package com.devfat.mini_ecommerce.shared;




import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/health")
@Tag(name = "Health")
@SecurityRequirements({})
public class PingController {
    // test cache layer

    // change 2
    @GetMapping
    public ResponseEntity<String> ping() {
        return ResponseEntity.ok("pong");
    }
}
