package maze.util;

/**
 * 搜索步骤类型
 * VISIT     - 探索访问一个节点
 * BACKTRACK - 回溯（死路，弹栈）
 * FOUND     - 找到终点
 */
public enum StepType {
    VISIT,
    BACKTRACK,
    FOUND,
    SHORTEST_PATH
}
