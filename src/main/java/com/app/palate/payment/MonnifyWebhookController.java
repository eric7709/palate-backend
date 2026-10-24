package com.app.palate.payment;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.app.palate.payment.services.HandleMonnifyWebhook;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/webhooks")
@RequiredArgsConstructor
public class MonnifyWebhookController {
    private final HandleMonnifyWebhook handleMonnifyWebhook;

    @PostMapping("/monnify")
    public ResponseEntity<Void> handleWebhook(@RequestBody String rawBody) {
        handleMonnifyWebhook.handle(rawBody);
        return ResponseEntity.ok().build();
    }
}
