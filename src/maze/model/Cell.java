package maze.model;

/**
 * 迷宫单元格
 * 
 * 封装单个格子的坐标、墙体、搜索状态等全部属性。
 * 每个 Cell 知道自己四面墙是否存在、是否被访问过、是否在路径上。
 */
public class Cell {

    /** 行坐标 */
    public final int row;
    /** 列坐标 */
    public final int col;

    /** 四面墙：true 表示有墙 */
    public boolean wallUp = true;
    public boolean wallDown = true;
    public boolean wallLeft = true;
    public boolean wallRight = true;

    /** 搜索过程中是否被访问过 */
    public boolean visited = false;
    /** 是否在最终路径上（BFS 最短路径 或 DFS 可行路径） */
    public boolean isPath = false;
    /** 是否被回溯过（DFS 回溯标记） */
    public boolean isBacktracked = false;

    /** 父节点，用于 BFS/DFS 路径恢复 */
    public Cell parent = null;

    /** 单元格类型：PATH / START / END */
    public CellType type;

    // ============ 构造方法 ============

    /**
     * @param row  行坐标
     * @param col  列坐标
     * @param type 单元格类型
     */
    public Cell(int row, int col, CellType type) {
        this.row = row;
        this.col = col;
        this.type = type;
    }

    // ============ 公共方法 ============

    /**
     * 重置搜索相关的状态（保留墙体信息和类型）。
     * 每次重新搜索前调用。
     */
    public void resetSearchState() {
        this.visited = false;
        this.isPath = false;
        this.isBacktracked = false;
        this.parent = null;
    }

    /**
     * 重置全部状态（包括墙体），用于重新生成迷宫时。
     */
    public void resetAll() {
        this.wallUp = true;
        this.wallDown = true;
        this.wallLeft = true;
        this.wallRight = true;
        this.visited = false;
        this.isPath = false;
        this.isBacktracked = false;
        this.parent = null;
    }

    @Override
    public String toString() {
        return String.format("Cell(%d,%d)", row, col);
    }
}