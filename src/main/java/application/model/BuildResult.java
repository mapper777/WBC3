package application.model;

import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BuildResult {
    private boolean forwardToParser;
    private String name;
    private String wUser;
    private List<List<String>> extension;
    private String scanner;
    private List<List<String>> syntax;
    private String template;
    private String errors;
    private Integer what;
    private int nd;
    private int cols;
    private List<String> ranges;
    private List<List<Integer>> table;

    private String statusMessage;
    private List<String> generatedText;
    private boolean isLL1;
    private List<RelationRow> relationRows;


    public void setIsLL1(boolean isLL1) {
        this.isLL1 = isLL1;
    }

    /** Заголовки для LL(1)-таблицы (case 14) */
    private List<String> ll1Headers;
    /** Строки LL(1)-таблицы */
    private List<LL1Row> ll1Rows;
    /** Имена действий (Actions) для LL(1)-таблицы */
    private List<String> actionNames;


    // … геттеры/сеттеры для ll1Headers, ll1Rows, actionNames …

    /**
     * Одна «ячейка» LL(1)-таблицы:
     *   - htmlFragment: содержимое ячейки (<table>…</table> или «Stop»/«^>» и т. д.)
     *   - cssClass: дополнительный CSS-класс (например, "lgr" или "bl"), если нужен конфликт
     *   - title: атрибут title (например, "i:j") или пустая строка
     */
    @Getter
    public static class LL1Cell {
        private final String htmlFragment;
        private final String cssClass;
        private final String title;

        public LL1Cell(String htmlFragment, String cssClass, String title) {
            this.htmlFragment = htmlFragment;
            this.cssClass = cssClass;
            this.title = title;
        }

        public LL1Cell(String htmlFragment, String cssClass) {
            this(htmlFragment, cssClass, "");
        }

    }

    /**
     * Одна «строка» LL(1)-таблицы:
     *   - rowIndex: номер строки (i-й символ в грамматике)
     *   - cells: список LL1Cell для каждого столбца (терминалов и нетерминалов)
     */
    @Getter
    public static class LL1Row {
        private final int rowIndex;
        private final List<LL1Cell> cells;

        public LL1Row(int rowIndex, List<LL1Cell> cells) {
            this.rowIndex = rowIndex;
            this.cells = cells;
        }

    }
    /**
     * Список строк таблицы множеств выбора.
     * Каждая строка описывает одно правило:
     *   - leftSymbol: имя нетерминала (левая часть)
     *   - selectSet: список имён символов в множестве выбора
     *   - conflict: true, если найден конфликт (пересечение множеств выбора)
     */
    private List<SelectRow> selectionRows;
    /** Максимальный размер какого-либо множества выбора (для разметки таблицы) */
    private int maxSelectSize;
    private List<Integer> tableHeaders;



    /** Строки таблицы: rowIndex и список Boolean (true—«*», false—пусто) */
    private List<BooleanRow> tableBooleanRows;

    @Getter
    public static class SelectRow {
        // имя нетерминала
        private final String leftSymbol;
        // список имен символов, входящих в множесво выбора для этого правила
        private final List<String> selectSet;
        // флаг, указывающий на конфликт
        private final boolean conflict;

        public SelectRow(String leftSymbol, List<String> selectSet, boolean conflict) {
            this.leftSymbol = leftSymbol;
            this.selectSet = selectSet;
            this.conflict = conflict;
        }
    }


    /**
     * Одна строка таблицы: rowIndex — номер строки,
     * values — список булевых, где true означает «*», а false — пустую ячейку.
     */
    @Getter
    public static class BooleanRow {
        private final int rowIndex;
        private final List<Boolean> values;

        public BooleanRow(int rowIndex, List<Boolean> values) {
            this.rowIndex = rowIndex;
            this.values = values;
        }

    }

//    /**
//     * Одна строка таблицы «отношений» (case 11 и 12 в buildParser.jsp).
//     * rowIndex   — индекс символа-строки (i),
//     * values     — по каждому j (столбцу) true→"*", false→пусто.
//     */
//    @Getter
//    public static class RelationRow {
//        private final int rowIndex;
//        private final List<Boolean> values;
//
//        public RelationRow(int rowIndex, List<Boolean> values) {
//            this.rowIndex = rowIndex;
//            this.values = values;
//        }
//
//    }

    /**
     * Одна строка «RelationRow» (для case 13/14 в buildParser.jsp).
     * Каждый объект хранит:
     *   rowSymbolName   — имя левого символа правила,
     *   flagA, flagS, flagR, flagE,
     *   transition      — номер перехода (stateIndex),
     *   selectSet       — список имён символов в множестве выбора,
     *   actionSymbol    — имя символа-действия (если первый символ type==0).
     */
    @Getter
    public static class RelationRow {
        private final String rowSymbolName;
        private final boolean flagA;
        private final boolean flagS;
        private final boolean flagR;
        private final boolean flagE;
        private final int transition;
        private final List<String> selectSet;
        private final String actionSymbol;

        public RelationRow(String rowSymbolName,
                           boolean flagA,
                           boolean flagS,
                           boolean flagR,
                           boolean flagE,
                           int transition,
                           List<String> selectSet,
                           String actionSymbol) {
            this.rowSymbolName = rowSymbolName;
            this.flagA = flagA;
            this.flagS = flagS;
            this.flagR = flagR;
            this.flagE = flagE;
            this.transition = transition;
            this.selectSet = selectSet;
            this.actionSymbol = actionSymbol;
        }

    }

    /** Список заголовков столбцов Управляющей LR-таблицы */
    private List<LRHeader> lrHeaders;

    /** Вложенный класс для одного заголовка LR-таблицы */
    @Getter
    public static class LRHeader {
        private final String symbolName;
        private final String cssClass;
        private final int type;

        public LRHeader(String symbolName, String cssClass, int type) {
            this.symbolName = symbolName;
            this.cssClass = cssClass;
            this.type = type;
        }

    }

    /** Список строк Управляющей LR-таблицы */
    private List<LRRow> lrRows;

    /** Одна строка LR-таблицы */
    @Getter
    public static class LRRow {
        /** Номер состояния (то есть номер строки) */
        private final int rowIndex;
        /** Список ячеек для этого состояния (по каждому заголовку из lrHeaders) */
        private final List<LRCell> cells;

        public LRRow(int rowIndex, List<LRCell> cells) {
            this.rowIndex = rowIndex;
            this.cells = cells;
        }

    }

    /** Одна ячейка LR-таблицы */
    @Getter
    public static class LRCell {
        /** HTML-текст ячейки (например, “S3”, “R2” или пустая строка) */
        private final String htmlFragment;
        /** CSS-класс: "", "lgr" (shift/reduce-конфликт), "bl" (reduce/reduce-конфликт) и т. п. */
        private final String cssClass;
        /** Атрибут title (например, "i:j") или пустая строка */
        private final String title;

        public LRCell(String htmlFragment, String cssClass, String title) {
            this.htmlFragment = htmlFragment;
            this.cssClass = cssClass;
            this.title = title;
        }

    }

    /** Список строк для «Таблицы расширенных конфигураций» (case 17) */
    private List<ExtConfigRow> extConfigRows;

    /**
     * Одна строка расширенных конфигураций (одно состояние).
     * Поля:
     *   stateIndex — номер состояния (i);
     *   entries    — список конфигураций (Rule + stateTo + контекст).
     */
    @Getter
    public static class ExtConfigRow {
        private final int stateIndex;
        private final List<ExtConfigCell> entries;

        public ExtConfigRow(int stateIndex, List<ExtConfigCell> entries) {
            this.stateIndex = stateIndex;
            this.entries = entries;
        }

    }

    /**
     * Один «элемент» в расширенной конфигурации (один MarkedRule в этом состоянии):
     *   ruleLeft  — имя левого нетерминала (если null, то "<Text>");
     *   markerPos — позиция «^» внутри правой части (MarkerIndex);
     *   stateTo   — переход (stateTo);
     *   context   — список символов (их имена) из контекста (селект-множества) этого MarkedRule.
     *   isCore    — true, если это базовая конфигурация (типа kernel), иначе false.
     */
    @Getter
    public static class ExtConfigCell {
        private final String ruleLeft;
        private final int markerPos;
        private final int stateTo;
        private final List<String> context;
        private final boolean isCore;

        public ExtConfigCell(String ruleLeft, int markerPos, int stateTo, List<String> context, boolean isCore) {
            this.ruleLeft = ruleLeft;
            this.markerPos = markerPos;
            this.stateTo = stateTo;
            this.context = context;
            this.isCore = isCore;
        }

    }


    // Добавляем поле для заголовков «канонической таблицы» (case 18)
    // В вашем случае headers — это просто List<String>.
    private List<String> confConfigHeaders;

    // А вот rows вы храните как List<ExtConfigRow>, потому что
    // ExtConfigRow описывает одну строку «расширенных конфигураций».
    private List<ExtConfigRow> confConfigRows;

    private List<String> extConfigHeaders;

}
