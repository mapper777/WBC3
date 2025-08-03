package application.service;

import application.model.BuildResult;
import application.work.finiteAutomat.FiniteAutomat;
import application.work.grammar.*;
import application.work.templateToAutomat.TemplateToAutomates;
import application.work.tplParser.BldParser;
import application.work.tplParser.PseudoCodeItem;
import application.work.tplParser.ReaderFromFile;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * — загрузка свойств шаблонов
 * — разбор «syntax» и построение Grammar
 * — обработка разных case (what = 0, 11/12, 13, 14, 15, 16, 17, 18)
 *
 * Входные параметры приходят в BuildResult (DTO), а не через HttpServletRequest.
 * Результаты (строки ошибок, флаги, таблицы и пр.) также записываются в BuildResult,
 * чтобы затем контроллер мог положить их в модель для Thymeleaf.
 */
@Service
public class ParserService {

    // private final String baseDir;

    public ParserService() {}

    /**
     * Основной метод, выполняющий логику buildParser.
     * Вход: BuildResult с заполненными полями:
     *   - what (Integer)
     *   - syntax (String)
     *   - template (String)
     *   - scanner (String)
     *   - extension (String)
     *   - name (String)
     *   - errors (String) — предыдущие ошибки (если есть)
     *   - wUser (String) — идентификатор пользователя
     *
     * После выполнения в buildResult:
     *   - buildResult.errors обновляется (если добавились новые сообщения)
     *   - buildResult.statusMessage заполняется (например, "Сканер и парсер построены…")
     *   - buildResult.generatedText заполняется списком строк с выходным модулем
     *   - buildResult.tableData и другие поля (если нужны) заполняются данными таблиц
     */
    public void buildParser(BuildResult buildResult) throws IOException {
        // 1) Приведём входные строки к нужному формату
        int what = parseWhat(buildResult.getWhat());
        String errStr = buildResult.getErrors() == null ? "" : buildResult.getErrors();
        String rawSyntax = buildResult.getSyntax().toString();
        String syntax = normalizeSyntax(rawSyntax);

        // 2) Загрузим Properties из templates/templateStrings.txt
        Properties templateStrings = loadTemplateStrings();

        // 3) Читаем файл шаблона .wtt
        String templateName = buildResult.getTemplate();
        Resource resource = new ClassPathResource("templates/" + templateName + ".wtt");
        if (!resource.exists()) {
            throw new FileNotFoundException("Template not found: " + templateName);
        }

        // Reader reader = new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8);

        ReaderFromFile reader = new ReaderFromFile("templates/" + templateName + ".wtt");

        // 4) Построим грамматику (пока без учёта case)
        Grammar grammar = buildGrammarFromSyntax(syntax);

        // 5) В зависимости от what, запускаем соответствующий хэндлер
        switch (what) {
            case 0:
                errStr = handleCase0(buildResult, templateName, templateStrings, reader, grammar, errStr);
                break;
            case 11:
            case 12:
                handleCase11_12(buildResult, what, grammar);
                break;
            case 13:
                handleCase13(buildResult, grammar);
                break;
            case 14:
                handleCase14(buildResult, grammar);
                break;
            case 15:
                handleCase15(buildResult, grammar);
                break;
            case 16:
                handleCase16(buildResult, grammar);
                break;
            case 17:
                handleCase17(buildResult, grammar);
                break;
            case 18:
                handleCase18(buildResult, grammar);
                break;
            default:
                // Если значение what некорректно, добавляем сообщение в ошибки
                errStr += " Неподдерживаемое значение what=" + what;
        }

        // 6) Сохраняем итоговую строку ошибок (если она изменилась) в DTO
        buildResult.setErrors(errStr);
    }

    // Парсит значение what из DTO (может быть Integer, String и т.д.)
    private int parseWhat(Object whatObj) {
        if (whatObj == null) return 0;
        try {
            return Integer.parseInt(whatObj.toString());
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    // Обрезает первые '[' и последние ']' у строки syntax, если нужно
    private String normalizeSyntax(String rawSyntax) {
        if (rawSyntax == null) return "";
        String trimmed = rawSyntax.trim();
        if (trimmed.length() > 2 && trimmed.startsWith("[") && trimmed.endsWith("]")) {
            return trimmed.substring(1, trimmed.length() - 1);
        }
        return trimmed;
    }

    // Загружает шаблонные строки из файла templateStrings.txt
    private Properties loadTemplateStrings() {
        Properties props = new Properties();
        String path = "templates/templateStrings.txt";
        try (FileReader fr = new FileReader(path)) {
            props.load(fr);
        } catch (IOException ignored) {
            // Если файл не найден — возвращаем пустые Properties
        }
        return props;
    }


    private Grammar buildGrammarFromSyntax(String syntax) {
        Grammar grammar = new Grammar();
        SymbolSet ss = grammar.getSymbolSet();

        if (syntax == null || syntax.isEmpty()) {
            return grammar;
        }
        String[] rulesArray = syntax.split("],");
        for (String ruleStr : rulesArray) {
            String content = ruleStr.trim();
            if (content.startsWith("[")) {
                content = content.substring(1);
            }
            content = content.replaceAll("\",\"", "\"\u0002\""); // временная замена внутри кавычек
            String[] parts = content.split(",");
            Rule rule = new Rule(ss.addSymbol(1, parts[0]));
            for (int j = 1; j < parts.length; j++) {
                String smb = parts[j].trim().replaceAll("\"\u0002\"", "\",\"");
                int type = 1;
                if (smb.charAt(0) == '{') {
                    // если это действие {…}
                    while (!smb.endsWith("}") && j + 1 < parts.length) {
                        j++;
                        smb += "," + parts[j].trim().replaceAll("\"\u0002\"", "\",\"");
                    }
                    type = 0;
                } else if (smb.charAt(0) == '"') {
                    type = -3; // слово
                } else if (smb.charAt(0) == '~') {
                    j++;
                    if (j < parts.length) {
                        smb = parts[j].trim();
                    }
                    type = -4; // ~символ
                }
                Symbol sym = ss.addSymbol(type, smb);
                rule.addSymbol(sym);
            }
            grammar.addRule(rule);
        }

        grammar.calcProperties();
        return grammar;
    }

    /**
     * case = 0: строим сканер и парсер через BldParser и TemplateToAutomates, сохраняем результат в файле.
     * Возвращает объединённую строку ошибок (errStr + новые сообщения).
     */
    private String handleCase0(
            BuildResult buildResult,
            String templateName,
            Properties tStrings,
            ReaderFromFile reader,
            Grammar grammar,
            String errStr
    ) throws IOException {
        // 1) Парсим шаблон и получаем «pCode»
        BldParser parser = new BldParser(reader, tStrings);
        parser.parse();
        @SuppressWarnings("unchecked")
        ArrayList<PseudoCodeItem> pCode = parser.getPCode();

        // 2) Снимаем параметры из buildResult
        String scanner = buildResult.getScanner();
        String ext = buildResult.getExtension() == null ? "" : buildResult.getExtension().toString().trim();
        if (ext.startsWith("[")) ext = ext.substring(1);
        if (ext.endsWith("]")) ext = ext.substring(0, ext.length() - 1);
        String wUser = buildResult.getWUser();
        String name = buildResult.getName();

        // 3) Создаём TemplateToAutomates и генерируем текст
        TemplateToAutomates tta = new TemplateToAutomates(grammar, scanner, wUser + "`" + ext);
        tta.fNames = name + "|" + templateName;

        List<String> generatedText = null;
        if (tta != null) {
            if (pCode.isEmpty()) {
                errStr += "Не удалось построить транслятор, не найден шаблон " + templateName;
            } else {
                generatedText = tta.makeModule(pCode);
                if (generatedText == null) {
                    generatedText = tta.getText();
                }
                if (generatedText == null) {
                    errStr += "Не удалось построить транслятор, ошибка в шаблоне " + templateName;
                } else {
                    // 4) Сохраняем сгенерированный текст в файл users/{wUser}translator.txt
                    String userDir = "users/";
//                    new File(userDir).mkdirs();
                    Path userPath = Paths.get("users");
                    Files.createDirectories(userPath);  // IOException, если не удалось
                    String outFile = userDir + wUser + "translator.txt";
                    System.out.println("Working dir: " + System.getProperty("user.dir"));
                    System.out.println("Users directory: " + userPath.toAbsolutePath());
                    try (BufferedWriter wr = new BufferedWriter(new OutputStreamWriter(
                            new FileOutputStream(outFile), StandardCharsets.UTF_8))) {
                        for (String line : generatedText) {
                            wr.write(line);
                            wr.newLine();
                        }
                    } catch (IOException e) {
                        errStr += "Ошибка записи файла перевода: " + e.getMessage();
                    }

                    // 5) Определяем фразу-статус по типу грамматики
                    String status;
                    if (!templateName.contains("LRx")) {
                        status = (grammar.getCountOfSymbols(6) == 1)
                                ? "Сканер и парсер построены, грамматика относится к классу LL(1)"
                                : "Сканер построен, вместо парсера – заглушка, грамматика НЕ относится к классу LL(1)";
                    } else {
                        int nd = grammar.getCountOfSymbols(7);
                        if ((nd & 8) != 0) {
                            status = "(условно), грамматика относится к классу LR(?), имеются не разрешенные конфликты";
                        } else if ((nd & 4) != 0) {
                            status = ", грамматика относится к классу LR(1)";
                        } else if ((nd & 2) != 0) {
                            status = ", грамматика относится к классу LALR(1)";
                        } else if ((nd & 1) != 0) {
                            status = ", грамматика относится к классу SLR(1)";
                        } else {
                            status = ", грамматика относится к классу LR(0)";
                        }
                        status = "Сканер и парсер построены" + status;
                    }
                    buildResult.setStatusMessage(status);
                    buildResult.setGeneratedText(generatedText);
                }
            }
        }
        return errStr;
    }

    /**
     * case = 11 или 12: строим таблицу предшествования (StartRel) или последования (FollowRel).
     * Кладём результат в buildResult.tableHeaders и tableRows (или отдельные поля).
     */
    private void handleCase11_12(BuildResult buildResult, int what, Grammar grammar) {
        @SuppressWarnings("unchecked")
        List<List<Symbol>> table = (what == 11)
                ? grammar.getStartRel()
                : grammar.getFollowRel();

        SymbolSet symbSet = grammar.getSymbolSet();
        int size = symbSet.getSize();

        // tableHeaders: список индексов символов, где type != 0 и type > -4
        List<Integer> headers = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            Symbol s = symbSet.getSymbol(i);
            int type = s.getType();
            if (type != 0 && type > -4) {
                headers.add(i);
            }
        }

        // tableRows: каждая строка — (i, List<Boolean>) где Boolean показывает есть ли "*" в ячейке
        List<BuildResult.BooleanRow> rows = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            Symbol s = symbSet.getSymbol(i);
            int type = s.getType();
            if (type != 0 && type > -4) {
                List<Symbol> related = (i < table.size()) ? table.get(i) : null;
                List<Boolean> rowMask = new ArrayList<>();
                for (int j : headers) {
                    if (what == 12 && i == j) {
                        rowMask.add(true); // диагональное "*"
                    } else {
                        rowMask.add(related != null && related.contains(symbSet.getSymbol(j)));
                    }
                }
                rows.add(new BuildResult.BooleanRow(i, rowMask));
            }
        }

        buildResult.setTableHeaders(headers);
        buildResult.setTableBooleanRows(rows);
    }

    /**
     * case = 13: множества выбора (SELECT-sets). Кладём в buildResult.selectionSets:
     *   List<BuildResult.SelectRow> с флагом неконфликтности.
     */
    private void handleCase13(BuildResult buildResult, Grammar grammar) {
        int ruleCount = grammar.getRulesCount();
        int maxSelSize = 0;
        for (int i = 0; i < ruleCount; i++) {
            List<Symbol> sel = grammar.getSelSet(grammar.getRule(i));
            if (sel.size() > maxSelSize) {
                maxSelSize = sel.size();
            }
        }

        List<BuildResult.SelectRow> selectRows = new ArrayList<>();
        Symbol curSymb = null;
        List<Symbol> selSet = new ArrayList<>();
        List<Symbol> rSelS = new ArrayList<>();

        for (int i = 0; i < ruleCount; i++) {
            Rule curRule = grammar.getRule(i);
            if (curRule == null) continue;

            if (curSymb == null || curRule.getLeftPart() != curSymb) {
                curSymb = curRule.getLeftPart();
                selSet.clear();
                rSelS.clear();
                List<Symbol> firstSel = grammar.getSelSet(curRule);
                selSet.addAll(firstSel);

                int kk = i + 1;
                while (kk < ruleCount) {
                    Rule next = grammar.getRule(kk);
                    if (next == null || next.getLeftPart() != curSymb) break;
                    List<Symbol> nextSel = next.getSelSet();
                    for (Symbol s : nextSel) {
                        if (!selSet.contains(s)) selSet.add(s);
                        else rSelS.add(s);
                    }
                    kk++;
                }
            }

            boolean conflict = false;
            List<Symbol> thisSel = grammar.getSelSet(curRule);
            for (Symbol s : rSelS) {
                if (thisSel.contains(s)) {
                    conflict = true;
                    break;
                }
            }

            List<String> items = new ArrayList<>();
            for (Symbol s : thisSel) {
                String name = s.getType() == -1 ? "EOF" : s.getName().replaceAll("<", "&lt;");
                items.add(name);
            }
            selectRows.add(new BuildResult.SelectRow(curSymb.getName(), items, conflict));
        }

        boolean isLL1 = grammar.testLL1();
        buildResult.setIsLL1(isLL1);
        buildResult.setSelectionRows(selectRows);
        buildResult.setMaxSelectSize(maxSelSize);
    }

    /**
     * case = 14: строит подробно LL(1) таблицу действий. Кладёт в buildResult.ll1Table и buildResult.actionNames.
     */
    private void handleCase14(BuildResult buildResult, Grammar grammar) {
        boolean isLL1 = grammar.testLL1();
        buildResult.setIsLL1(isLL1);

        if (!isLL1) {
            return;
        }

        int cntN = grammar.getCountOfSymbols(1);
        int cntT = grammar.getCountOfSymbols(2);
        int cntA = grammar.getCountOfSymbols(3);
        int cntS = cntN + cntT;

        SymbolSet symbSet = grammar.getSymbolSet();

        // Заголовок: [EOF] + терминалы(2..cntS-1) + Aktionen
        List<String> headers = new ArrayList<>();
        headers.add("EOF");
        for (int i = cntN + 1; i < cntA; i++) {
            Symbol s = symbSet.getSymbol(i);
            if (s.getType() == -3) {
                headers.add("<i>" + s.getName() + "</i>");
            } else if (i < cntS) {
                headers.add(s.getName());
            } else if (s.getType() != -4 && s.getName() != null && !s.getName().isEmpty()) {
                headers.add("A" + (i - cntS));
            }
        }

        // Строки LL(1): для каждого символа i (0..cntA-1, тип != -4)
        List<BuildResult.LL1Row> rows = new ArrayList<>();
        for (int i = 0; i < cntA; i++) {
            Symbol rowSym = symbSet.getSymbol(i);
            if (rowSym.getType() == -4) continue;

            List<BuildResult.LL1Cell> cells = new ArrayList<>();
            for (int k = cntN; k < cntA; k++) {
                Symbol colSym = symbSet.getSymbol(k);
                if (colSym.getType() == -4) continue;

                // Для нетерминалов (i < cntN) заполняем действия по правилам
                if (i < cntN) {
                    boolean found = false;
                    for (int ii = 0; ii < grammar.getRulesCount(); ii++) {
                        Rule rule = grammar.getRule(rowSym, ii);
                        if (rule == null) break;
                        List<Symbol> sel = grammar.getSelSet(rule);
                        for (Symbol s : sel) {
                            if (s == colSym) {
                                // Если правило подходит, формируем мини-таблицу
                                StringBuilder cellHtml = new StringBuilder();
                                cellHtml.append("<table class='tb'>");
                                cellHtml.append("<tr><td colspan='2'><span class='df'>^</span></td></tr>");
                                if (rule.getLength() > 1 || rule.getSymbol(0).getId() >= 0) {
                                    cellHtml.append("<tr><td class='df' rowspan='")
                                            .append(rule.getLength())
                                            .append("'>!</td>");
                                    for (int jj = 0; jj < rule.getLength(); jj++) {
                                        Symbol sym = rule.getSymbol(jj);
                                        if (jj > 0) cellHtml.append("<tr>");
                                        if (sym.getType() != 0) {
                                            cellHtml.append("<td")
                                                    .append(sym.getType() == -3 ? " class='di'" : "")
                                                    .append(">")
                                                    .append(sym.getName())
                                                    .append(sym.getType() > 0 ? "</b>" : "")
                                                    .append("</td>");
                                        } else {
                                            // символ – действие из cntS..cntA-1
                                            for (int ndx = 0; ndx < cntA; ndx++) {
                                                if (sym == symbSet.getSymbol(ndx)) {
                                                    cellHtml.append("<td class='lgr'>A")
                                                            .append(ndx - cntS)
                                                            .append("</td>");
                                                    break;
                                                }
                                            }
                                        }
                                        cellHtml.append("</tr>");
                                    }
                                }
                                cellHtml.append("</table>");
                                cells.add(new BuildResult.LL1Cell(cellHtml.toString(), ""));
                                found = true;
                                break;
                            }
                        }
                        if (found) break;
                    }
                    if (!found) {
                        // пустая ячейка
                        cells.add(new BuildResult.LL1Cell("", ""));
                    }
                }
                // Для нетерминала той же позиции (i == k) ставим Stop
                else if (i == k) {
                    if (i == cntN) {
                        cells.add(new BuildResult.LL1Cell("<span style='color:red'>Stop</span>", ""));
                    } else {
                        String actionSymbol = (i < cntS ? ">" : "Exec");
                        cells.add(new BuildResult.LL1Cell("<span class='df'>^" + actionSymbol + "</span>", ""));
                    }
                } else {
                    cells.add(new BuildResult.LL1Cell("", ""));
                }
            }
            rows.add(new BuildResult.LL1Row(i, cells));
        }

        // Действия (action names):
        List<String> actionNames = new ArrayList<>();
        for (int i = cntS; i < cntA; i++) {
            Symbol act = symbSet.getSymbol(i);
            if (act.getType() == 0) {
                actionNames.add(act.getName());
            }
        }

        buildResult.setLl1Headers(headers);
        buildResult.setLl1Rows(rows);
        buildResult.setActionNames(actionNames);
    }

    /**
     * case = 15: вычисляем Relationship таблицу (N-flag_A-flag_S-flag_R-flag_E) и выборы переходов.
     */
    private void handleCase15(BuildResult buildResult, Grammar grammar) {
        boolean isLL1 = grammar.testLL1();
        buildResult.setIsLL1(isLL1);

        if (!isLL1) {
            return;
        }

        SymbolSet symbSet = grammar.getSymbolSet();
        int cntN = grammar.getCountOfSymbols(1);
        int cntT = grammar.getCountOfSymbols(2);

        List<BuildResult.RelationRow> relRows = new ArrayList<>();
        int k = 0;
        int ruleCount = grammar.getRulesCount();
        Symbol currentLeft = null;

        // Сначала собираем наборы правил по одному нетерминалу (groupBy LeftPart)
        for (int i = 0; i < ruleCount; i++) {
            // ничего, только считаем максимальную длину (см. case 13)
        }

        // Теперь строим сами строки:
        for (int kidx = 0; kidx < ruleCount; kidx++) {
            Rule r = grammar.getRule(kidx);
            Symbol left = r.getLeftPart();
            List<Integer> rowData = new ArrayList<>();
            // Флаг_A: если в правиле есть действие (type=0) справа
            boolean flagA = false;
            // Флаг_S: если next symbol exists
            boolean flagS = (r.getLength() > 1);
            // Флаг_R: если ни след. символа нет и текущий type<=0
            boolean flagR = false;
            // Флаг_E: если тип curSym.type≤0 и следующий null (тип окончания)
            boolean flagE = false;

            for (int j = 0; j < r.getLength(); j++) {
                Symbol tsym = r.getSymbol(j);
                int t = tsym.getType();
                if (t == 0) {
                    flagA = true;
                }
                if (j + 1 < r.getLength()) {
                    // есть следующий символ
                }
            }
            if (!flagS && !flagA) {
                flagE = true;
            }

            // Номер перехода = номер stateTo для этого правила (предполагается 0-й RSymbol)
            int transition = r.getRSymbol(0).getStateIndex();

            // Множество выбора (Select): выводим список символов
            List<String> sel = new ArrayList<>();
            for (Symbol s : grammar.getSelSet(r)) {
                String name = (s.getType() == -1) ? "EOF" : s.getName().replaceAll("<", "&lt;");
                sel.add(name);
            }

            // Действие: если r.getSymbol(0).type == 0 (первый символ действие), выводим его имя
            String actionSym = "";
            if (r.getLength() > 0 && r.getSymbol(0).getType() == 0) {
                actionSym = r.getSymbol(0).getName();
            }

            relRows.add(new BuildResult.RelationRow(left.getName(), flagA, flagS, flagR, flagE, transition, sel, actionSym));
        }

        buildResult.setRelationRows(relRows);
    }

    /**
     * case = 16: строим LR-требования (конфликты shift/reduce, reduce/reduce).
     * Кладём в buildResult.lrHeader, lrRows, actionDecoding.
     */
    private void handleCase16(BuildResult buildResult, Grammar grammar) {
        String[][] lrT = grammar.getLRTable();
        int nd = grammar.getCountOfSymbols(7);

        // Определяем класс LR
        String grammarClass;
        if ((nd & 8) != 0) {
            grammarClass = "Грамматика относится к классу LR(?), имеются не разрешенные конфликты";
        } else if ((nd & 4) != 0) {
            grammarClass = "Грамматика относится к классу LR(1)";
        } else if ((nd & 2) != 0) {
            grammarClass = "Грамматика относится к классу LALR(1)";
        } else if ((nd & 1) != 0) {
            grammarClass = "Грамматика относится к классу SLR(1)";
        } else {
            grammarClass = "Грамматика относится к классу LR(0)";
        }
        buildResult.setStatusMessage(grammarClass);

        SymbolSet symbSet = grammar.getSymbolSet();
        int size = symbSet.getSize();

        // Заголовок: по символьным типам
        List<BuildResult.LRHeader> headers = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            Symbol s = symbSet.getSymbol(i);
            int type = s.getType();
            if (type != 0 && type > -4) {
                String name = s.getName().replaceAll("<", "&lt;");
                String cssClass = "";
                if (type > 0) {
                    cssClass = "bd";
                } else if (type == -1) {
                    name = "EOF";
                } else if (type == -3) {
                    cssClass = "dfm"; // или «italic, green»
                }
                headers.add(new BuildResult.LRHeader(name, cssClass, type));
            }
        }

        // Строки: (stateIndex, List<LRCell>)
        List<BuildResult.LRRow> rows = new ArrayList<>();
        int rowCount = lrT.length;
        int conflictMax = 0;
        // Сначала найдём max k, чтобы понять, нужны ли стили «lgr», «bl»
        for (int i = 0; i < rowCount; i++) {
            for (int j = 0; j < lrT[i].length; j++) {
                String rab = lrT[i][j] == null ? "" : lrT[i][j];
                int k = 0;
                for (int p = 0; p < rab.length(); p++) {
                    if (rab.charAt(p) == 'S')       k |= 1;
                    if (rab.charAt(p) == 'R') {
                        if (k < 2) k |= 2;
                        else       k = 7;
                    }
                    if (k >= 3) break;
                }
                conflictMax = Math.max(conflictMax, k);
            }
        }

        for (int i = 0; i < rowCount; i++) {
            List<BuildResult.LRCell> cells = new ArrayList<>();
            for (int j = 0; j < lrT[i].length; j++) {
                String rab = lrT[i][j] == null ? "" : lrT[i][j];
                int k = 0;
                for (int p = 0; p < rab.length(); p++) {
                    if (rab.charAt(p) == 'S')       k |= 1;
                    if (rab.charAt(p) == 'R') {
                        if (k < 2) k |= 2;
                        else       k = 7;
                    }
                    if (k >= 3) break;
                }
                String title = rab.isEmpty() ? "" : (i + ":" + j);
                String cssClass = "";
                if      (k >= 3 && k == 3) cssClass = "lgr";
                else if (k >= 3 && k >  3) cssClass = "bl";

                String displayValue = rab.isEmpty() ? "" : rab + "\u00A0";
                cells.add(new BuildResult.LRCell(displayValue, cssClass, title));
            }
            rows.add(new BuildResult.LRRow(i, cells));
        }

        // Действия (если есть)
        @SuppressWarnings("unchecked")
        List<String> actionList = grammar.getActions();
        buildResult.setActionNames(actionList);
        buildResult.setLrHeaders(headers);
        buildResult.setLrRows(rows);
    }

    /**
     * case = 17: Таблица расширенных конфигураций. Кладём в buildResult.extConfigHeaders, extConfigRows.
     */
    private void handleCase17(BuildResult buildResult, Grammar grammar) {
        @SuppressWarnings("unchecked")
        List<List<MarkedRule>> uniConf = grammar.getUniConfTbl();
        SymbolSet symbSet = grammar.getSymbolSet();

        // Заголовки: Состояние, Переход, ОПК, Маркированное правило
        List<String> headers = List.of("Состояние", "Переход", "ОПК", "Маркированное правило");

        // Строки: BuildResult.ExtConfigRow { stateIndex, List<BuildResult.ExtConfigCell> }
        List<BuildResult.ExtConfigRow> rows = new ArrayList<>();
        for (int i = 0; i < uniConf.size(); i++) {
            List<MarkedRule> rulesInState = uniConf.get(i);
            List<BuildResult.ExtConfigCell> cells = new ArrayList<>();
            for (MarkedRule mr : rulesInState) {
                if (mr == null) continue;
                Rule curRule = mr.getRule();
                int stateTo = mr.getStateTo();

                // Контекст: OПK (лист символов)
                List<String> contextList = new ArrayList<>();
                List<Symbol> ctx = mr.getContext();
                if (ctx != null && !ctx.isEmpty()) {
                    for (Symbol s : ctx) {
                        String name = s == null ? "EOF" : s.getName().replaceAll("<", "&lt;");
                        if (name.isEmpty()) name = "EOF";
                        contextList.add(name);
                    }
                }

                // Левый нетерминал (или "<Text>")
                Symbol left = curRule.getLeftPart();
                String leftName = (left == null) ? "<Text>" : left.getName().replaceAll("<", "&lt;");

                // Строка правила с маркером '^'
                int markerIndex = mr.getMarkerIndex();
                boolean isCore = (mr.getType() != 0);
                List<String> ruleWithMarker = new ArrayList<>();
                for (int k = 0; k < curRule.getLength(); k++) {
                    if (k == markerIndex) {
                        ruleWithMarker.add("^");
                    }
                    Symbol sym = curRule.getSymbol(k);
                    if (sym != null) {
                        String nm = sym.getName().replaceAll("<", "&lt;");
                        ruleWithMarker.add(nm);
                    }
                }
                if (curRule.getLength() <= markerIndex) {
                    ruleWithMarker.add("^");
                }

//                BuildResult.ExtConfigCell cell = new BuildResult.ExtConfigCell(
//                        stateTo, contextList, leftName, ruleWithMarker, mr.getType() != 0
//                );

                BuildResult.ExtConfigCell cell = new BuildResult.ExtConfigCell(
                        leftName,      // String ruleLeft
                        markerIndex,     // int markerPos
                        stateTo,       // int stateTo
                        contextList,   // List<String> context
                        isCore         // boolean isCore
                );
                cells.add(cell);
            }
            rows.add(new BuildResult.ExtConfigRow(i, cells));
        }

        buildResult.setExtConfigHeaders(headers);
        buildResult.setExtConfigRows(rows);
    }

    /**
     * case = 18: Таблица канонических конфигураций. Аналогично расширенной, но без OПK-списка.
     */
    private void handleCase18(BuildResult buildResult, Grammar grammar) {
        @SuppressWarnings("unchecked")
        List<List<MarkedRule>> conf = grammar.getConfTable();
        SymbolSet symbSet = grammar.getSymbolSet();

        List<String> headers = List.of("Состояние", "Переход", "ОПК", "Маркированное правило");
        List<BuildResult.ExtConfigRow> rows = new ArrayList<>();

        for (int i = 0; i < conf.size(); i++) {
            List<MarkedRule> rulesInState = conf.get(i);
            List<BuildResult.ExtConfigCell> cells = new ArrayList<>();
            for (MarkedRule mr : rulesInState) {
                if (mr == null) continue;
                Rule curRule = mr.getRule();
                int stateTo = mr.getStateTo();

                // Контекст: первый символ или EOF
                String firstContext = "";
                List<Symbol> ctx = mr.getContext();
                if (ctx != null && !ctx.isEmpty()) {
                    Symbol s = ctx.get(0);
                    firstContext = (s == null) ? "EOF" : s.getName().replaceAll("<", "&lt;");
                    if (firstContext.isEmpty()) firstContext = "EOF";
                }

                // Левый нетерминал
                Symbol left = curRule.getLeftPart();
                String leftName = (left == null) ? "<Text>" : left.getName().replaceAll("<", "&lt;");

                // Правило с маркером '^'
                int markerIndex = mr.getMarkerIndex();
                List<String> ruleWithMarker = new ArrayList<>();
                for (int k = 0; k < curRule.getLength(); k++) {
                    if (k == markerIndex) {
                        ruleWithMarker.add("^");
                    }
                    Symbol sym = curRule.getSymbol(k);
                    if (sym != null) {
                        String nm = sym.getName().replaceAll("<", "&lt;");
                        ruleWithMarker.add(nm);
                    }
                }
                if (curRule.getLength() <= markerIndex) {
                    ruleWithMarker.add("^");
                }

                BuildResult.ExtConfigCell cell = new BuildResult.ExtConfigCell(
                        leftName,       // String  ruleLeft
                        markerIndex,    // int markerPos
                        stateTo,        // int stateTo
                        List.of(firstContext),    // List<String> context
                        mr.getType() != 1         // boolean isCore
                );

//                BuildResult.ExtConfigCell cell = new BuildResult.ExtConfigCell(
//                        stateTo, List.of(firstContext), leftName, ruleWithMarker, mr.getType() != 1
//                );
                cells.add(cell);
            }
            rows.add(new BuildResult.ExtConfigRow(i, cells));
        }

        buildResult.setConfConfigHeaders(headers);
        buildResult.setConfConfigRows(rows);
    }
}






