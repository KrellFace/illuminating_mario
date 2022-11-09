package illuminating_mario.metricExtraction;

public enum enum_MarioMetrics {
    
    //Structural Metrics
    Contiguity,
    AdjustedContiguity,
    Linearity,
    Density,
    ClearRows,
    ClearColumns,

    //Block Counts
    PipeCount,
    BlockCount,
    EmptySpace,
    EnemyCount,
    RewardCount,
    
    //Agent Extracted Metrics
    Playability,
    JumpCount,
    JumpCountByPlayability,
    JumpEntropy,
    Speed,
    TimeTaken,
    TotalKills,
    KillsByStomp,
    KillsOverEnemies,
    MaxJumpAirTime

}
