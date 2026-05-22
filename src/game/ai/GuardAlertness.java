package game.ai;

/**
 * Global alertness levels shared across all guards. Used to make guards
 * react more strongly when one of them spots the player.
 */
public enum GuardAlertness {
    RELAXED,
    SUSPICIOUS,
    ALERT
}

