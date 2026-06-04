package maze.ui;

import maze.model.Cell;
import maze.model.CellType;
import maze.model.Maze;

import javax.swing.*;
import java.awt.*;

/**
 * 迷宫绘制组件
 * 
 * 负责将 Maze 数据渲染为图形。
 * 遍历 Cell[][]，根据墙体信息画线段，根据搜索状态填色。
 * 支持窗口缩放时自动重算格子尺寸。
 * 
 * 颜色规范（来自任务书）：
 *   墙体 - 黑色      通路 - 白色
 *   起点 - 绿色      终点 - 红色
 *   探索 - 蓝色      回溯 - 橙色
 *   路径 - 黄色      背景 - 浅灰
 */
public class MazePanel extends JPanel {

    /** 当前显示的迷宫 */
    private Maze maze;

    /** 单元格边长（像素），随窗口大小动态计算 */
    private int cellSize;

    /** 迷宫在面板中的偏移量，用于居中显示 */
    private int offsetX;
    private int offsetY;

    // ============ 颜色常量 ============

    private static final Color COLOR_WALL       = Color.BLACK;
    private static final Color COLOR_PATH       = Color.WHITE;
    private static final Color COLOR_START      = new Color(76, 175, 80);   // 绿色
    private static final Color COLOR_END        = new Color(244, 67, 54);   // 红色
    private static final Color COLOR_VISITED    = new Color(100, 149, 237); // 蓝色
    private static final Color COLOR_BACKTRACKED = new Color(255, 165, 0);  // 橙色
    private static final Color COLOR_FINAL_PATH = new Color(255, 235, 59);  // 黄色
    private static final Color COLOR_BACKGROUND = new Color(220, 220, 220); // 浅灰

    // ============ 边界常量 ============

    /** 迷宫四周留白（像素） */
    private static final int PADDING = 20;
    /** 最小格子尺寸 */
    private static final int MIN_CELL_SIZE = 8;
    /** 墙体线宽 */
    private static final int WALL_WIDTH = 2;

    // ============ 公共方法 ============

    /**
     * 设置要绘制的迷宫，设置后自动刷新。
     */
    public void setMaze(Maze maze) {
        this.maze = maze;
        recalculateSize();
        repaint();
    }

    /**
     * 获取当前迷宫。
     */
    public Maze getMaze() {
        return maze;
    }

    // ============ 绘制 ============

    // 绘制流程：背景 → 格子填色 → 墙体线段
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                             RenderingHints.VALUE_ANTIALIAS_ON);

        // 背景
        g2d.setColor(COLOR_BACKGROUND);
        g2d.fillRect(0, 0, getWidth(), getHeight());

        if (maze == null) return;

        recalculateSize();
        drawCells(g2d);
        drawWalls(g2d);
    }

    // ============ 绘制辅助方法 ============

    /**
     * 绘制所有格子的填充色。
     * 绘制顺序：通路 → 已访问 → 回溯 → 路径 → 起点 → 终点
     * 后画的覆盖先画的，确保叠加效果正确。
     */
    private void drawCells(Graphics2D g) {
        for (int r = 0; r < maze.rows; r++) {
            for (int c = 0; c < maze.cols; c++) {
                Cell cell = maze.grid[r][c];
                int x = offsetX + c * cellSize;
                int y = offsetY + r * cellSize;

                // 确定填充颜色（按优先级叠加）
                Color fill = getCellFillColor(cell);
                g.setColor(fill);
                g.fillRect(x, y, cellSize, cellSize);
            }
        }
    }

    /**
     * 按优先级决定格子的填充颜色。
     */
    private Color getCellFillColor(Cell cell) {
        // 起点和终点优先级最高
        if (cell.type == CellType.START)  return COLOR_START;
        if (cell.type == CellType.END)    return COLOR_END;

        // 最终路径（黄色）覆盖已访问
        if (cell.isPath) return COLOR_FINAL_PATH;

        // 回溯标记（橙色）
        if (cell.isBacktracked) return COLOR_BACKTRACKED;

        // 已访问（蓝色）
        if (cell.visited) return COLOR_VISITED;

        // 普通通路
        return COLOR_PATH;
    }

    /**
     * 绘制所有墙体线段。
     * 每个格子只画右侧墙和下侧墙，避免重复绘制。
     */
    private void drawWalls(Graphics2D g) {
        g.setColor(COLOR_WALL);
        g.setStroke(new BasicStroke(WALL_WIDTH));

        for (int r = 0; r < maze.rows; r++) {
            for (int c = 0; c < maze.cols; c++) {
                Cell cell = maze.grid[r][c];
                int x = offsetX + c * cellSize;
                int y = offsetY + r * cellSize;

                // 上墙（仅第一行）
                if (r == 0 && cell.wallUp) {
                    g.drawLine(x, y, x + cellSize, y);
                }
                // 左墙（仅第一列）
                if (c == 0 && cell.wallLeft) {
                    g.drawLine(x, y, x, y + cellSize);
                }
                // 下墙
                if (cell.wallDown) {
                    g.drawLine(x, y + cellSize, x + cellSize, y + cellSize);
                }
                // 右墙
                if (cell.wallRight) {
                    g.drawLine(x + cellSize, y, x + cellSize, y + cellSize);
                }
            }
        }
    }

    // ============ 布局计算 ============

    /**
     * 根据面板大小重新计算格子尺寸和偏移量。
     */
    private void recalculateSize() {
        if (maze == null) return;

        int availWidth = getWidth() - 2 * PADDING;
        int availHeight = getHeight() - 2 * PADDING;

        cellSize = Math.min(availWidth / maze.cols, availHeight / maze.rows);
        if (cellSize < MIN_CELL_SIZE) cellSize = MIN_CELL_SIZE;

        offsetX = (getWidth() - maze.cols * cellSize) / 2;
        offsetY = (getHeight() - maze.rows * cellSize) / 2;
    }
}