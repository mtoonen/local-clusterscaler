package nl.meine.scaler.controller;

import nl.meine.scaler.up.WakeOnLan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.nio.file.StandardWatchEventKinds;

@RestController
public class ScaleUpController {

    private WakeOnLan wol;

    @Autowired
    public ScaleUpController(WakeOnLan wol) {
        this.wol = wol;
    }

    @GetMapping(path = "up")
    public String up(@RequestParam String macAddress) {
        try {
            wol.wake(macAddress);
        } catch (IOException e) {
            return "No: " + e.getMessage();
        }
        return "yes";
    }


    @GetMapping(path = "down")
    public String down(@RequestParam String ipAddress) {
        return "no";
    }


}
