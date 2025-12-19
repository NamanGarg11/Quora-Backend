package com.example.QuoraApp.events;

import com.example.QuoraApp.enums.TargetType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ViewCountEvent {
    private String targetId;
    private TargetType targetType;
    private LocalDateTime localDateTime;
}
