package com.tms.restapi.toolsmanagement.kit.controller;


import com.tms.restapi.toolsmanagement.kit.model.Kit;
import com.tms.restapi.toolsmanagement.kit.service.KitService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/kits")
public class KitController {

    @Autowired
    private KitService kitService;

    @PostMapping("/create")
    public ResponseEntity<Kit> createKit(@RequestBody Kit kit, @RequestParam List<Long> toolIds) {
        return ResponseEntity.ok(kitService.createKit(kit, toolIds));
    }

    @GetMapping("/all")
    public ResponseEntity<List<Kit>> getAllKits() {
        return ResponseEntity.ok(kitService.getAllKits());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Kit> getKitById(@PathVariable Long id) {
        return kitService.getKitById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<Kit> updateKit(@PathVariable Long id, @RequestBody Kit kit, @RequestParam List<Long> toolIds) {
        return ResponseEntity.ok(kitService.updateKit(id, kit, toolIds));
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<String> deleteKit(@PathVariable Long id) {
        return ResponseEntity.ok(kitService.deleteKit(id));
    }
}