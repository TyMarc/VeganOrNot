package com.lesgens.veganornot.listeners;

import java.util.List;

public interface VeganOrNotCheckListener {
    void onCheckFailed();

    void onCheckDone(List<String> nonVegans, List<String> canBeVegan);
}
