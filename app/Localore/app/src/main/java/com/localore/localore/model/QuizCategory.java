package com.localore.localore.model;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.PrimaryKey;
import android.graphics.drawable.Icon;

/**
 * A quiz-category of a certain exercise.
 */
@Entity
public class QuizCategory {

    /**
     * The different types of quiz-categories.
     */
    public static final String[] types = new String[]{
            "settlements",
            "roads",
            "nature",
            "transport",
            "constructions"};
    public static final int SETTLEMENTS = 0;
    public static final int ROADS = 1;
    public static final int NATURE = 2;
    public static final int TRANSPORT = 3;
    public static final int CONSTRUCTIONS = 4;


    @PrimaryKey(autoGenerate = true)
    private long id;

    /**
     * Exercise of this quiz-category.
     */
    private long exerciseId;

    /**
     * Quiz-category type.
     */
    private int type;

    /**
     * Number of category reminder-quizzes to complete.
     */
    private int requiredCategoryReminders = 0;

    /**
     * @param exerciseId
     * @param type
     */
    public QuizCategory(long exerciseId, int type) {
        this.exerciseId = exerciseId;
        this.type = type;
    }

    /**
     * @return Icon representing category.
     */
    public Icon getIcon() {
        switch(this.type) {
            case 0: return null;
            case 1: return null;
            case 2: return null;
            case 3: return null;
            case 4: return null;
            default: throw new RuntimeException("Deda-end");
        }
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getExerciseId() {
        return exerciseId;
    }

    public void setExerciseId(long exerciseId) {
        this.exerciseId = exerciseId;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getRequiredCategoryReminders() {
        return requiredCategoryReminders;
    }

    public void setRequiredCategoryReminders(int requiredCategoryReminders) {
        this.requiredCategoryReminders = requiredCategoryReminders;
    }

    @Override
    public String toString() {
        switch(this.type) {
            case 0: return "Settlements";
            case 1: return "Roads";
            case 2: return "Nature";
            case 3: return "Transport";
            case 4: return "Constructions";
            default: throw new RuntimeException("Deda-end");
        }
    }
}
