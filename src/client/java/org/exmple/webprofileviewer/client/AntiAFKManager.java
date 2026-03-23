package org.exmple.webprofileviewer.client;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.phys.Vec3;

import java.util.Random;

public class AntiAFKManager {
    private static final Random RANDOM = new Random();

    // Configurable switches
    private static boolean enabled = false;

    // Session state
    private static boolean wasActive = false;
    private static int activeMovementTicks = 0;
    private static int nextSmartMoveTick = 0;
    private static boolean isSmartMoving = false;
    private static Direction smartMoveDirection = Direction.FORWARD;
    private static int smartMoveDuration = 0;
    private static int smartMoveStartTick = 0;
    private static Vec3 smartMoveStartPos = null;

    private enum Direction {
        FORWARD,
        RIGHT,
        BACKWARD,
        LEFT
    }

    public static void setEnabled(boolean newState) {
        enabled = newState;
        if (!enabled) {
            resetAndStop();
        }
    }

    public static void update(LocalPlayer player) {
        if (player == null) {
            resetAndStop();
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        if (!enabled) {
            resetAndStop();
            return;
        }
        if (mc.options == null) {
            return;
        }
        if (mc.screen != null) {
            forceStopAll(mc);
            return;
        }

        wasActive = true;
        activeMovementTicks++;

        if (nextSmartMoveTick == 0) {
            nextSmartMoveTick = activeMovementTicks + 20; // ~1s grace before first move
        }

        if (isSmartMoving) {
            if (activeMovementTicks >= smartMoveStartTick + smartMoveDuration) {
                isSmartMoving = false;
                forceStopAll(mc);
                boolean moved = hasMoved(player);
                nextSmartMoveTick = activeMovementTicks + (moved ? 1000 + RANDOM.nextInt(401) : 2);
                smartMoveStartPos = null;
            } else {
                applyDirection(mc, smartMoveDirection);
            }
        } else {
            if (activeMovementTicks >= nextSmartMoveTick) {
                startNewMove(mc, player);
            } else {
                forceStopAll(mc);
            }
        }
    }

    private static void startNewMove(Minecraft mc, LocalPlayer player) {
        isSmartMoving = true;
        smartMoveStartTick = activeMovementTicks;
        smartMoveStartPos = player.position();
        smartMoveDuration = 10 + RANDOM.nextInt(11); // 10-20 ticks (~0.5-1s)
        smartMoveDirection = Direction.values()[RANDOM.nextInt(Direction.values().length)];
        applyDirection(mc, smartMoveDirection);
    }

    private static void applyDirection(Minecraft mc, Direction direction) {
        if (mc.options == null) return;
        setKeyState(mc.options.keyUp, direction == Direction.FORWARD);
        setKeyState(mc.options.keyRight, direction == Direction.RIGHT);
        setKeyState(mc.options.keyDown, direction == Direction.BACKWARD);
        setKeyState(mc.options.keyLeft, direction == Direction.LEFT);
    }

    private static void forceStopAll(Minecraft mc) {
        if (mc.options == null) return;
        setKeyState(mc.options.keyUp, false);
        setKeyState(mc.options.keyRight, false);
        setKeyState(mc.options.keyDown, false);
        setKeyState(mc.options.keyLeft, false);
    }

    private static void setKeyState(KeyMapping key, boolean pressed) {
        if (key != null) {
            key.setDown(pressed);
        }
    }

    private static boolean hasMoved(LocalPlayer player) {
        if (smartMoveStartPos == null) return false;
        double dx = player.getX() - smartMoveStartPos.x;
        double dz = player.getZ() - smartMoveStartPos.z;
        return dx * dx + dz * dz > 1.0;
    }

    private static void resetAndStop() {
        if (wasActive) {
            forceStopAll(Minecraft.getInstance());
        }
        wasActive = false;
        isSmartMoving = false;
        activeMovementTicks = 0;
        nextSmartMoveTick = 0;
        smartMoveStartPos = null;
    }
}
