package com.WarriorPoses.iigastudio.controllers;

import com.WarriorPoses.iigastudio.models.WarriorPose;
import com.WarriorPoses.iigastudio.repositories.WarriorPoseRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Optional;

@RestController
@RequestMapping("/warrior-poses")
public class WarriorPosesController {


        @Autowired
        WarriorPoseRepository warriorPoseRepository;

    @GetMapping("/")
    public Iterable<WarriorPose> getAllWarriorPoses() {
        return warriorPoseRepository.findAll();
    }

    @GetMapping("/search/{keyword}")
    public ResponseEntity<WarriorPose> getWarriorPoseById(@PathVariable String keyword) {
        Optional<WarriorPose> warriorPose = warriorPoseRepository.findByNameIgnoreCaseContaining(keyword);
        if (warriorPose.isPresent()) {
            return ResponseEntity.ok(warriorPose.get());
        } else {
            return ResponseEntity.notFound().build();
        }

    }

        @GetMapping("/search/variation/{keyword}")
    public Optional<WarriorPose> searchWarriorPoseByVariationName(@PathVariable String keyword) {
        return warriorPoseRepository.findByVariationNameIgnoreCaseContaining(keyword);
    }
    @GetMapping("/{id}")
        public ResponseEntity<WarriorPose> getWarriorPoseById(@PathVariable Long id) {
            Optional<WarriorPose> warriorPose = warriorPoseRepository.findById(id);
            if (warriorPose.isPresent()) {
                return ResponseEntity.ok(warriorPose.get());
            } else {
                return ResponseEntity.notFound().build();
            }
        }

    private String getImageUrl(HttpServletRequest request, String imagePath) {
        String baseUrl = getBaseUrl(request);
        return baseUrl + "/images/" + getFileName(imagePath);
    }

    private String getBaseUrl(HttpServletRequest request) {
        String baseUrl = request.getRequestURL().toString();
        return baseUrl.replace(request.getServletPath(), "");
    }

    private String getFileName(String imagePath) {
        return imagePath.substring(imagePath.lastIndexOf("/") + 1);
    }

    @PostMapping("/")
    public ResponseEntity<WarriorPose> createWarriorPose(HttpServletRequest request, @RequestParam("image") MultipartFile imageFile, @RequestParam("name") String poseName,@RequestParam("description") String description) {
        String userRole = request.getHeader("X-User-Role");

        if ("ADMIN".equals(userRole)) {
            try {
                String imageName = imageFile.getOriginalFilename();
                String projectDir = System.getProperty("user.dir");
                String imagePath = projectDir + "/images/" + imageName;
                File image = new File(imagePath);

                // Save the image file
                image.getParentFile().mkdirs(); // Create parent directories if they don't exist
                image.createNewFile();
                try (OutputStream outputStream = new FileOutputStream(image)) {
                    outputStream.write(imageFile.getBytes());
                }

                // Create a new WarriorPose object and set the image path and name
                WarriorPose warriorPose = new WarriorPose();
                warriorPose.setName(poseName);
                warriorPose.setDescription(description);
                warriorPose.setImageUrl(getImageUrl(request, imagePath)); // Set the image URL

                WarriorPose dbWarriorPose = warriorPoseRepository.save(warriorPose);
                return ResponseEntity.ok(dbWarriorPose);
            } catch (Exception e) {
                e.printStackTrace();
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
            }
        } else {
            // Handle unauthorized access
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }
    @PutMapping("/{id}")
    public ResponseEntity<WarriorPose> updateWarriorPose(HttpServletRequest request, @PathVariable Long id, @RequestParam("image") MultipartFile imageFile, @RequestParam("name") String poseName, @RequestParam("description") String description) {
        String userRole = request.getHeader("X-User-Role");

        if ("ADMIN".equals(userRole)) {
            try {
                String imageName = imageFile.getOriginalFilename();
                String projectDir = System.getProperty("user.dir");
                String imagePath = projectDir + "/images/" + imageName;
                File image = new File(imagePath);

                // Save the image file
                image.getParentFile().mkdirs(); // Create parent directories if they don't exist
                image.createNewFile();
                try (OutputStream outputStream = new FileOutputStream(image)) {
                    outputStream.write(imageFile.getBytes());
                }

                // Find the existing WarriorPose object
                Optional<WarriorPose> optionalWarriorPose = warriorPoseRepository.findById(id);
                if (optionalWarriorPose.isPresent()) {
                    WarriorPose warriorPose = optionalWarriorPose.get();

                    // Update the WarriorPose attributes
                    warriorPose.setName(poseName);
                    warriorPose.setDescription(description);
                    warriorPose.setImageUrl(getImageUrl(request, imagePath)); // Set the image URL

                    WarriorPose dbWarriorPose = warriorPoseRepository.save(warriorPose);
                    return ResponseEntity.ok(dbWarriorPose);
                } else {
                    return ResponseEntity.notFound().build();
                }
            } catch (Exception e) {
                e.printStackTrace();
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
            }
        } else {
            // Handle unauthorized access
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }
        @DeleteMapping("/{id}")
        public ResponseEntity<?> deleteWarriorPose(HttpServletRequest request, @PathVariable Long id) {
            String userRole = request.getHeader("X-User-Role");

            if ("ADMIN".equals(userRole)) {
                Optional<WarriorPose> optionalWarriorPose = warriorPoseRepository.findById(id);
                if (optionalWarriorPose.isPresent()) {
                    warriorPoseRepository.delete(optionalWarriorPose.get());
                    return ResponseEntity.ok().build();
                } else {
                    return ResponseEntity.notFound().build();
                }
            } else {
                // Handle unauthorized access
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

        }


    }

