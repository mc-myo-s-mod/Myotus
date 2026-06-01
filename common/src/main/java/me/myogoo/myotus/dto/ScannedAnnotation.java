package me.myogoo.myotus.dto;

import org.objectweb.asm.Type;

import java.lang.annotation.ElementType;

public record ScannedAnnotation(Type annotationType, ElementType targetType, Type targetClass) {
}
