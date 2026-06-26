package maze.model;

import maze.util.StepType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 搜索步骤记录
 * 
 * 搜索算法每走一步都生成一个 SearchStep，
 * 动画播放器按序读取并驱动界面更新。
 */
public class SearchStep {

    /** 本步操作的格子 */
    public final Cell cell;

    /** 步骤类型：探索 / 回溯 / 找到终点 */
    public final StepType type;

    /** 步骤序号，从 1 开始 */
    public final int stepNumber;

    /** 可选：当前步骤需要高亮的完整路径快照 */
    public final List<Cell> pathCells;

    public SearchStep(Cell cell, StepType type, int stepNumber) {
        this(cell, type, stepNumber, null);
    }

    public SearchStep(Cell cell, StepType type, int stepNumber, List<Cell> pathCells) {
        this.cell = cell;
        this.type = type;
        this.stepNumber = stepNumber;
        if (pathCells == null) {
            this.pathCells = Collections.emptyList();
        } else {
            this.pathCells = Collections.unmodifiableList(new ArrayList<>(pathCells));
        }
    }

    @Override
    public String toString() {
        return String.format("[%d] %s %s", stepNumber, type, cell);
    }
}
