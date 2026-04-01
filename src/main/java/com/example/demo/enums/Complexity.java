package com.example.demo.enums;


public enum Complexity {
    LOW, MEDIUM, HIGH;

    public int getWeight() {
        return switch (this) {
            case LOW -> 1;
            case MEDIUM -> 2;
            case HIGH -> 3;
        };
    }
}