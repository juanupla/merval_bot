package developBot.MervalOperations.controllers;

import developBot.MervalOperations.services.mervalBotService.BotMervalService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/botMerval")
public class botMervalController {

    @Autowired
    private BotMervalService botMervalService;

    @GetMapping("/start")
    public ResponseEntity<Boolean> startBot(){
        return ResponseEntity.ok(true);
    }

}
