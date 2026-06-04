package maze.util;

import maze.model.Cell;
import maze.model.CellType;
import maze.model.Maze;

import java.io.*;
import java.nio.charset.StandardCharsets;

/**
 * 迷宫文件读写
 * 
 * 支持将迷宫保存为文本文件，或从文件导入迷宫。
 * 
 * 文件格式：
 *   # 注释行（可选，以 # 开头）
 *   rows cols
 *   startRow startCol
 *   endRow endCol
 *   然后 rows 行，每行 cols 个整数（0-15），
 *   每个整数表示一格的四墙配置：
 *     bit 0 (1) = 上墙
 *     bit 1 (2) = 下墙
 *     bit 2 (4) = 左墙
 *     bit 3 (8) = 右墙
 * 
 * 示例（5×5 迷宫）：
 *   5 5
 *   0 0
 *   4 4
 *   7 13 9 11 3
 *   5 10 12 6 15
 *   ...
 */
public class MazeIO {

    /**
     * 将迷宫导出到文件。
     *
     * @param maze 要导出的迷宫
     * @param file 目标文件
     * @throws IOException 写入失败时抛出
     */
    public static void save(Maze maze, File file) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(
                new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8))) {

            // 头部：尺寸 + 起终点
            writer.write(maze.rows + " " + maze.cols);
            writer.newLine();
            writer.write(maze.start.row + " " + maze.start.col);
            writer.newLine();
            writer.write(maze.end.row + " " + maze.end.col);
            writer.newLine();

            // 每行每格的四墙配置
            for (int r = 0; r < maze.rows; r++) {
                StringBuilder line = new StringBuilder();
                for (int c = 0; c < maze.cols; c++) {
                    if (c > 0) line.append(" ");
                    line.append(cellToInt(maze.grid[r][c]));
                }
                writer.write(line.toString());
                writer.newLine();
            }
        }
    }

    /**
     * 从文件导入迷宫。
     *
     * @param file 迷宫数据文件
     * @return 导入的 Maze 对象
     * @throws IOException 读取失败或格式错误时抛出
     */
    public static Maze load(File file) throws IOException {
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {

            // 跳过注释行
            String line = reader.readLine();
            while (line != null && line.trim().startsWith("#")) {
                line = reader.readLine();
            }

            // 第一行：尺寸
            if (line == null) throw new IOException("文件为空");
            String[] dims = line.trim().split("\\s+");
            int rows = Integer.parseInt(dims[0]);
            int cols = Integer.parseInt(dims[1]);

            // 第二行：起点
            line = reader.readLine();
            String[] startPos = line.trim().split("\\s+");
            int startR = Integer.parseInt(startPos[0]);
            int startC = Integer.parseInt(startPos[1]);

            // 第三行：终点
            line = reader.readLine();
            String[] endPos = line.trim().split("\\s+");
            int endR = Integer.parseInt(endPos[0]);
            int endC = Integer.parseInt(endPos[1]);

            // 创建迷宫
            Maze maze = new Maze(rows, cols);

            // 读墙体数据
            for (int r = 0; r < rows; r++) {
                line = reader.readLine();
                if (line == null) throw new IOException("墙体数据不足，期望 " + rows + " 行");
                String[] wallStrs = line.trim().split("\\s+");
                if (wallStrs.length != cols)
                    throw new IOException("第 " + (r + 4) + " 行：期望 " + cols + " 个值，实际 " + wallStrs.length);

                for (int c = 0; c < cols; c++) {
                    int config = Integer.parseInt(wallStrs[c]);
                    applyWalls(maze.grid[r][c], config);
                }
            }


            // 设置起点终点
            maze.setStartEnd(startR, startC, endR, endC);

            return maze;
        }
    }

    // ============ 私有辅助方法 ============

    /**
     * 将格子的四墙配置编码为 0-15 的整数。
     */
    private static int cellToInt(Cell cell) {
        int v = 0;
        if (cell.wallUp)    v |= 1;
        if (cell.wallDown)  v |= 2;
        if (cell.wallLeft)  v |= 4;
        if (cell.wallRight) v |= 8;
        return v;
    }

    /**
     * 将整数解码并应用到格子的四墙上。
     */
    private static void applyWalls(Cell cell, int config) {
        cell.wallUp    = (config & 1) != 0;
        cell.wallDown  = (config & 2) != 0;
        cell.wallLeft  = (config & 4) != 0;
        cell.wallRight = (config & 8) != 0;
    }
}