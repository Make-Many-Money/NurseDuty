package org.example;

import java.util.*;

public class Main {
    public static Map<String, List<Integer>> filterShiftCandidates(
            Map<Integer, List<String>> nurseAvailability,
            Map<Integer, List<String>> last5DaysShifts
    ) {
        Map<String, List<Integer>> shiftCandidates = new HashMap<>();
        List<String> shiftTypes = Arrays.asList("D", "E", "N", "O");
        for (String shift : shiftTypes) {
            shiftCandidates.put(shift, new ArrayList<>());
        }


        for (Map.Entry<Integer, List<String>> entry : nurseAvailability.entrySet()) {
            int nurse = entry.getKey();
            List<String> availableShifts = new ArrayList<>(entry.getValue());
            List<String> last5Days = last5DaysShifts.getOrDefault(nurse, new ArrayList<>());

            // 연속 5일 근무 제한
            if (!last5Days.contains("O")) {
                availableShifts = Arrays.asList("O");
            }

            // 근무 아래로 꺾이는것 금지
            if (last5Days.get(last5Days.size() - 1).equals("N")) {
                availableShifts.removeAll(Arrays.asList("D", "E"));
            }
            if (last5Days.get(last5Days.size() - 1).equals("E")) {
                availableShifts.removeAll(Arrays.asList("D"));
            }

            // 나이트 근무 2~3일 연속
            if (last5Days.get(last5Days.size() - 1).equals("N")
                    && last5Days.get(last5Days.size() - 2).equals("N")
                    && last5Days.get(last5Days.size() - 3).equals("N")
            ) {
                availableShifts = Arrays.asList("O");
            }

            if (last5Days.get(last5Days.size() - 1).equals("N")
                    && !last5Days.get(last5Days.size() - 2).equals("N")
            ) {
                availableShifts = Arrays.asList("N");
            }

            // 가능한 근무 타입에 따라 shiftCandidates에 추가
            for (String shift : availableShifts) {
                shiftCandidates.get(shift).add(nurse);
            }
        }
        System.out.println(shiftCandidates);
        return shiftCandidates;
    }

    public static List<Map<String, List<Integer>>> generateShiftCombinations(
            Map<String, List<Integer>> shiftCandidates,
            Map<String, Integer> neededPerShift
    ) {
        List<String> shiftTypes = new ArrayList<>(shiftCandidates.keySet());
        List<Map<String, List<Integer>>> allCombinations = new ArrayList<>();
        backtrack(0, new HashMap<>(), shiftTypes, new HashMap<>(shiftCandidates), neededPerShift, allCombinations);
        return allCombinations;
    }

    private static void backtrack(int index, Map<String, List<Integer>> currentCombination, List<String> shiftTypes,
                                  Map<String, List<Integer>> shiftCandidates, Map<String, Integer> neededPerShift,
                                  List<Map<String, List<Integer>>> allCombinations) {
        if (index == shiftTypes.size()) {
            allCombinations.add(new HashMap<>(currentCombination));
            return;
        }

        String shift = shiftTypes.get(index);
        List<Integer> candidates = shiftCandidates.get(shift);
        int needed = neededPerShift.getOrDefault(shift, 0);

        if (candidates.size() < needed) {
            return;
        }

        List<List<Integer>> combinations = getCombinations(candidates, needed);

        for (List<Integer> combo : combinations) {
            currentCombination.put(shift, combo);
            Map<String, List<Integer>> updatedCandidates = new HashMap<>();
            for (String key : shiftCandidates.keySet()) {
                if (!key.equals(shift)) {
                    List<Integer> updatedList = new ArrayList<>(shiftCandidates.get(key));
                    updatedList.removeAll(combo);
                    updatedCandidates.put(key, updatedList);
                } else {
                    updatedCandidates.put(key, candidates);
                }
            }
            backtrack(index + 1, currentCombination, shiftTypes, updatedCandidates, neededPerShift, allCombinations);
            currentCombination.remove(shift);
        }
    }

    private static List<List<Integer>> getCombinations(List<Integer> candidates, int needed) {
        List<List<Integer>> result = new ArrayList<>();
        generateCombinations(candidates, needed, 0, new ArrayList<>(), result);
        return result;
    }

    private static void generateCombinations(
            List<Integer> candidates,
            int needed,
            int start,
            List<Integer> temp,
            List<List<Integer>> result
    ) {
        if (candidates.size() < needed) {
            return;
        }

        if (temp.size() == needed) {
            result.add(new ArrayList<>(temp));
            return;
        }
        for (int i = start; i < candidates.size(); i++) {
            temp.add(candidates.get(i));
            generateCombinations(candidates, needed, i + 1, temp, result);
            temp.remove(temp.size() - 1);
        }
    }

    public static void main(String[] args) {
        Map<Integer, List<String>> nurseAvailability = new HashMap<>();
        nurseAvailability.put(1, Arrays.asList("D", "E", "O"));
        nurseAvailability.put(2, Arrays.asList("N", "O"));
        nurseAvailability.put(3, Arrays.asList("E", "N", "O"));
        nurseAvailability.put(4, Arrays.asList("D", "E", "N", "O"));
        nurseAvailability.put(5, Arrays.asList("D", "E", "N", "O"));

        Map<Integer, List<String>> last5DaysShifts = new HashMap<>();
        last5DaysShifts.put(1, Arrays.asList("D", "D", "D", "E", "E"));
        last5DaysShifts.put(2, Arrays.asList("N", "N", "N", "O", "O"));
        last5DaysShifts.put(3, Arrays.asList("E", "E", "E", "O", "O"));
        last5DaysShifts.put(4, Arrays.asList("O", "O", "O", "D", "D"));
        last5DaysShifts.put(5, Arrays.asList("O", "O", "O", "N", "N"));

        Map<String, Integer> neededPerShift = new HashMap<>();
        neededPerShift.put("D", 1);
        neededPerShift.put("E", 1);
        neededPerShift.put("N", 1);

        Map<String, List<Integer>> shiftCandidates = filterShiftCandidates(nurseAvailability, last5DaysShifts);

        List<Map<String, List<Integer>>> combinationsList = generateShiftCombinations(shiftCandidates, neededPerShift);

        for (Map<String, List<Integer>> comb : combinationsList) {
            System.out.println(comb);
        }
    }
}
