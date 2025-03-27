package net.firestarter03.ctlive_afktimer.config;

import dev.isxander.yacl3.api.ConfigCategory;
import dev.isxander.yacl3.api.Option;
import dev.isxander.yacl3.api.OptionDescription;
import dev.isxander.yacl3.api.OptionGroup;
import dev.isxander.yacl3.api.YetAnotherConfigLib;
import dev.isxander.yacl3.api.controller.IntegerFieldControllerBuilder;
import dev.isxander.yacl3.api.controller.BooleanControllerBuilder;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

public class CTLiveAFKTimerConfigScreen {
    public static Screen create(Screen parent) {
        CTLiveAFKTimerConfig config = CTLiveAFKTimerConfig.load();

        return YetAnotherConfigLib.createBuilder()
                .title(Text.translatable("ctlive_afktimer.config.title"))
                .category(ConfigCategory.createBuilder()
                        .name(Text.translatable("ctlive_afktimer.config.general"))
                        .group(OptionGroup.createBuilder()
                                .name(Text.translatable("ctlive_afktimer.config.group"))
                                .description(OptionDescription.of(Text.translatable("ctlive_afktimer.config.group.description")))
                                .option(Option.<Integer>createBuilder()
                                        .name(Text.translatable("ctlive_afktimer.config.afkTimeout"))
                                        .description(OptionDescription.of(Text.translatable("ctlive_afktimer.config.afkTimeout.tooltip")))
                                        .binding(5, () -> config.afkTimeout, newValue -> {
                                            config.afkTimeout = newValue;
                                            config.save();
                                        })
                                        .controller(IntegerFieldControllerBuilder::create)
                                        .build())
                                .build())
                        .build())
                .category(ConfigCategory.createBuilder()
                        .name(Text.translatable("ctlive_afktimer.config.language"))
                        .group(OptionGroup.createBuilder()
                                .name(Text.translatable("ctlive_afktimer.config.language.group"))
                                .description(OptionDescription.of(Text.translatable("ctlive_afktimer.config.language.group.description")))
                                .option(Option.<Boolean>createBuilder()
                                        .name(Text.translatable("ctlive_afktimer.config.useEnglishAFKMessage"))
                                        .description(OptionDescription.of(Text.translatable("ctlive_afktimer.config.useEnglishAFKMessage.tooltip")))
                                        .binding(true, () -> config.useEnglishAFKMessage, newValue -> {
                                            config.useEnglishAFKMessage = newValue;
                                            config.save();
                                        })
                                        .controller(opt -> BooleanControllerBuilder.create(opt)
                                                .valueFormatter(val -> val ? Text.literal("§aEnglish") : Text.literal("§cDeutsch"))
                                                .coloured(true))
                                        .build())
                                .build())
                        .build())
                .save(config::save)
                .build()
                .generateScreen(parent);
    }
}
