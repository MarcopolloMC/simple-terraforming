package com.marcopolo.terraformmod;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.SliderWidget;
import net.minecraft.text.Text;

@Environment(EnvType.CLIENT)
public class TerraformScreen extends Screen {
    private static final int PADDING = 20;
    private SliderWidget rangeXSlider;
    private SliderWidget rangeYSlider;
    private SliderWidget rangeZSlider;
    private int currentTab = 0;
    private int scrollOffset = 0;

    public TerraformScreen() {
        super(Text.literal("Terraform Mod"));
    }

    @Override
    protected void init() {
        super.init();
        this.clearChildren();

        int sliderWidth = 200;
        int sliderX = this.width / 2 - sliderWidth / 2;

        // Range Slider X
        rangeXSlider = new SliderWidget(sliderX, 80, sliderWidth, 20, 
                Text.literal("Range X: "), TerraformHandler.getRangeX() / (double) TerraformHandler.getMaxRange()) {
            @Override
            protected void updateMessage() {
                this.setMessage(Text.literal("Range X: " + (int)(this.value * TerraformHandler.getMaxRange())));
            }

            @Override
            protected void applyValue() {
                TerraformHandler.setRange(
                    (int)(this.value * TerraformHandler.getMaxRange()),
                    TerraformHandler.getRangeY(),
                    TerraformHandler.getRangeZ()
                );
            }
        };

        // Range Slider Y
        rangeYSlider = new SliderWidget(sliderX, 110, sliderWidth, 20,
                Text.literal("Range Y: "), TerraformHandler.getRangeY() / (double) TerraformHandler.getMaxRange()) {
            @Override
            protected void updateMessage() {
                this.setMessage(Text.literal("Range Y: " + (int)(this.value * TerraformHandler.getMaxRange())));
            }

            @Override
            protected void applyValue() {
                TerraformHandler.setRange(
                    TerraformHandler.getRangeX(),
                    (int)(this.value * TerraformHandler.getMaxRange()),
                    TerraformHandler.getRangeZ()
                );
            }
        };

        // Range Slider Z
        rangeZSlider = new SliderWidget(sliderX, 140, sliderWidth, 20,
                Text.literal("Range Z: "), TerraformHandler.getRangeZ() / (double) TerraformHandler.getMaxRange()) {
            @Override
            protected void updateMessage() {
                this.setMessage(Text.literal("Range Z: " + (int)(this.value * TerraformHandler.getMaxRange())));
            }

            @Override
            protected void applyValue() {
                TerraformHandler.setRange(
                    TerraformHandler.getRangeX(),
                    TerraformHandler.getRangeY(),
                    (int)(this.value * TerraformHandler.getMaxRange())
                );
            }
        };

        this.addDrawableChild(rangeXSlider);
        this.addDrawableChild(rangeYSlider);
        this.addDrawableChild(rangeZSlider);

        // Tab Buttons
        this.addDrawableChild(ButtonWidget.builder(Text.literal("Range"), button -> currentTab = 0)
                .dimensions(this.width / 2 - 105, 40, 100, 20)
                .build());

        this.addDrawableChild(ButtonWidget.builder(Text.literal("Blöcke"), button -> {
            currentTab = 1;
            this.init();
        })
                .dimensions(this.width / 2 + 5, 40, 100, 20)
                .build());

        // Schließ-Button
        this.addDrawableChild(ButtonWidget.builder(Text.literal("Schließen"), button -> this.close())
                .dimensions(this.width / 2 - 50, this.height - 30, 100, 20)
                .build());

        if (currentTab == 1) {
            initBlockTab();
        }
    }

    private void initBlockTab() {
        // Block Tab mit Buttons für Blöcke
        int buttonWidth = 180;
        int buttonHeight = 20;
        int x = this.width / 2 - buttonWidth / 2;
        int y = 70;

        Block[] commonBlocks = {
            Blocks.DIRT, Blocks.GRASS_BLOCK, Blocks.STONE, Blocks.SAND,
            Blocks.GRAVEL, Blocks.GRANITE, Blocks.DIORITE, Blocks.ANDESITE,
            Blocks.DEEPSLATE, Blocks.COARSE_DIRT, Blocks.ROOTED_DIRT, Blocks.RED_SAND
        };

        for (int i = 0; i < commonBlocks.length && y < this.height - 50; i++) {
            Block block = commonBlocks[i];
            boolean isWhitelisted = TerraformHandler.getBlockWhitelist().contains(block);
            String text = (isWhitelisted ? "✓ " : "  ") + block.getName().getString();

            this.addDrawableChild(ButtonWidget.builder(Text.literal(text), button -> {
                if (isWhitelisted) {
                    TerraformHandler.removeBlockFromWhitelist(block);
                } else {
                    TerraformHandler.addBlockToWhitelist(block);
                }
                this.init();
            })
                    .dimensions(x, y, buttonWidth, buttonHeight)
                    .build());

            y += 25;
        }
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        this.renderBackground(context, mouseX, mouseY, delta);
        context.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2, 10, 0xFFFFFF);

        if (currentTab == 0) {
            context.drawTextWithShadow(this.textRenderer, 
                    Text.literal("Terraforming: " + (TerraformHandler.isTerraformingActive() ? "§aAN (T)" : "§cAUS (T)")),
                    PADDING, 180, 0xFFFFFF);
        }

        super.render(context, mouseX, mouseY, delta);
    }

    @Override
    public boolean shouldPauseGame() {
        return false;
    }

    @Override
    public void close() {
        this.client.setScreen(null);
    }
}
