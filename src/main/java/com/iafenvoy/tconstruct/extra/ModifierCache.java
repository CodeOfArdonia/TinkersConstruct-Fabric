package com.iafenvoy.tconstruct.extra;

import slimeknights.tconstruct.library.modifiers.ModifierManager;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public final class ModifierCache {

  public static final List<Runnable> RUN_AFTER_LOAD_MODIFIERS = new CopyOnWriteArrayList<>();

  static {
    ModifierManager.ModifiersLoadedEvent.EVENT.register(event -> {
      ModifierCache.RUN_AFTER_LOAD_MODIFIERS.forEach(Runnable::run);
      ModifierCache.RUN_AFTER_LOAD_MODIFIERS.clear();
    });
  }
}
