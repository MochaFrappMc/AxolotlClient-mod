/*
 * Copyright © 2024 moehreag <moehreag@gmail.com> & Contributors
 *
 * This file is part of AxolotlClient.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 *
 * For more information, see the LICENSE file.
 */

package io.github.axolotlclient.modules.hud.gui.hud;

import java.util.List;

import io.github.axolotlclient.AxolotlClientConfig.api.options.Option;
import io.github.axolotlclient.AxolotlClientConfig.impl.options.BooleanOption;
import io.github.axolotlclient.AxolotlClientConfig.impl.options.ColorOption;
import io.github.axolotlclient.AxolotlClientConfig.impl.options.EnumOption;
import io.github.axolotlclient.modules.hud.gui.component.DynamicallyPositionable;
import io.github.axolotlclient.modules.hud.gui.entry.TextHudEntry;
import io.github.axolotlclient.modules.hud.gui.layout.AnchorPoint;
import io.github.axolotlclient.modules.hud.gui.layout.Justification;
import io.github.axolotlclient.modules.hud.util.DefaultOptions;
import io.github.axolotlclient.modules.hud.util.DrawPosition;
import io.github.axolotlclient.modules.hud.util.Rectangle;
import io.github.axolotlclient.util.ClientColors;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.resource.Identifier;

/**
 * This implementation of Hud modules is based on KronHUD.
 * <a href="https://github.com/DarkKronicle/KronHUD">Github Link.</a>
 *
 * @license GPL-3.0
 */

public class StatusBarHud extends TextHudEntry implements DynamicallyPositionable {

	public static final Identifier ID = new Identifier("axolotlclient", "statusbarhud");

	protected final EnumOption<Justification> justification = new EnumOption<>("justification", Justification.class,
		Justification.CENTER);
	protected final EnumOption<AnchorPoint> anchor = DefaultOptions.getAnchorPoint();

	private final Rectangle graph = new Rectangle(0, 0, 0, 0);
	private final ColorOption healthbarColor = new ColorOption("healthbarColor",
		ClientColors.SELECTOR_RED.withAlpha(255));
	private final ColorOption backgroundBarColor = new ColorOption("backgroundBarColor",
		ClientColors.BLACK.withAlpha(255));
	private final ColorOption breathColor = new ColorOption("breathColor",
		ClientColors.SELECTOR_BLUE.withAlpha(255));

	private final BooleanOption showBar = new BooleanOption("showBar", true);
	private final BooleanOption showText = new BooleanOption("showText", false);
	private final BooleanOption showAllocated = new BooleanOption("showAllocated", false);

	private final float[] healthHistory = new float[10];
	private float smoothedHealth = 20.0f;


	public StatusBarHud() {
		super(90, 18, false);
	}

	@Override
	public void renderComponent(float delta) {
		DrawPosition pos = getPos();

		if (showBar.get()) {
			graph.setData(pos.x + 5, pos.y + 5, getBounds().width - 10, getBounds().height - 10);


			int healthHistoryIndex = 0;
			float delayedHealth = healthHistory[(healthHistoryIndex + 1) % healthHistory.length];
			float absorption = client.player.getAbsorption();
			float maxAbsorption = 20;
			float health = client.player.getHealth();
			float currentHealth = client.player.getHealth();
			float maxHealth = client.player.getMaxHealth();
			float lerpSpeed = 0.05f;

			smoothedHealth += (currentHealth - smoothedHealth) * lerpSpeed;

			float delayedHealthNormalized = smoothedHealth / maxHealth;
			float healthUsage = currentHealth / maxHealth;

			fill((int) (graph.x + graph.width * healthUsage), graph.y, graph.x + graph.width, graph.y + graph.height, backgroundBarColor.get().toInt());
			fill(graph.x, graph.y, (int) (graph.x + graph.width * delayedHealthNormalized), graph.y + graph.height, ClientColors.WHITE.withAlpha(255).toInt());
			fill(graph.x, graph.y, (int) (graph.x + graph.width * healthUsage), graph.y + graph.height, healthbarColor.get().toInt());

			if (absorption > 0) {
				fill(graph.x, graph.y, (int) (graph.x + graph.width * (absorption / maxAbsorption)), graph.y + graph.height,
					ClientColors.GOLD.withAlpha(255).toInt());
			}


		}

		if (showBar.get()) {
			graph.setData(pos.x + 5, pos.y - 5, getBounds().width - 10, getBounds().height - 10);

			float currentArmor = client.player.getArmorProtection();
			float maxArmor = 20;

			if (currentArmor > 0) {
				fill(graph.x, graph.y, (int) (graph.x + graph.width), graph.y + graph.height,
					backgroundBarColor.get().toInt());

				fill(graph.x, graph.y, (int) (graph.x + graph.width * (currentArmor / maxArmor)), graph.y + graph.height,
					ClientColors.GRAY
						.withAlpha(255).toInt());


			}
		}

		if (showBar.get()) {
			graph.setData(pos.x + 105, pos.y + 5, getBounds().width - 10, getBounds().height - 10);

			float currentHunger = client.player.getHungerManager().getFoodLevel();
			float maxHunger = 20;

			if (currentHunger > 0) {
				fill(graph.x, graph.y, (int) (graph.x + graph.width), graph.y + graph.height,
					ClientColors.GOLD.withAlpha(255).toInt());

				fill(graph.x, graph.y, (int) (graph.x + graph.width * ((20 - currentHunger) / maxHunger)), graph.y + graph.height,
					backgroundBarColor.get().toInt());
			}
		}

		if (showBar.get()) {
			graph.setData(pos.x + 105, pos.y - 5, getBounds().width - 10, getBounds().height - 10);

			float currentBreath = client.player.getBreath();
			float maxBreath = 300;

			if (currentBreath < 300) {
				fill(graph.x, graph.y, (int) (graph.x + graph.width), graph.y + graph.height,
					breathColor.get().toInt());

				fill(graph.x, graph.y, (int) (graph.x + graph.width * ((300 - currentBreath) / maxBreath)), graph.y + graph.height,
					backgroundBarColor.get().toInt());
			}
		}

		if (showText.get()) {
			int currentHealth = (int) client.player.getHealth();
			int maxHealth = (int) client.player.getMaxHealth();
			int absorptionHealth = (int) client.player.getAbsorption();
			int currentHunger = client.player.getHungerManager().getFoodLevel();
			int currentBreath = client.player.getBreath();
			int breathPercentage = (currentBreath * 100) / 300;
			int currentArmor = client.player.getArmorProtection();

			float totalHealth = currentHealth + absorptionHealth;

			String health = String.valueOf(client.player.getHealth());
			String formattedHealth = (totalHealth % 1 == 0) ? String.format("%.0f", totalHealth) : String.format("%.1f", totalHealth);
			String armor = String.valueOf(client.player.getArmorProtection());

			drawString((formattedHealth + "/" + maxHealth), pos.x + justification.get().getXOffset(client.textRenderer.getWidth(armor), getWidth() - 4) - 26,
				pos.y + (Math.round((float) height / 2) - 4) - (showAllocated.get() ? 4 : 0),
				textColor.get().toInt(), shadow.get());

			if (currentArmor > 0) {
				drawString((armor + "/" + 20), pos.x + justification.get().getXOffset(client.textRenderer.getWidth(armor), getWidth() - 4) - 26,
					pos.y + (Math.round((float) height / 2) - 14) - (showAllocated.get() ? 4 : 0),
					textColor.get().toInt(), shadow.get());
			}

			drawString((currentHunger + "/" + 20), pos.x + justification.get().getXOffset(client.textRenderer.getWidth((char) maxHealth), getWidth() - 4) + 114,
				pos.y + (Math.round((float) height / 2) - 4) - (showAllocated.get() ? 4 : 0),
				textColor.get().toInt(), shadow.get());

			if (currentBreath < 300) {
				drawString(String.valueOf((breathPercentage + "%")), pos.x + justification.get().getXOffset(client.textRenderer.getWidth((char) maxHealth), getWidth() - 4) + 126,
					pos.y + (Math.round((float) height / 2) - 14) - (showAllocated.get() ? 4 : 0),
					textColor.get().toInt(), shadow.get());
			}
		}
	}

	@Override
	public void renderPlaceholderComponent(float delta) {
		DrawPosition pos = getPos();

		if (showBar.get()) {
			graph.setData(pos.x + 5, pos.y + 5, getBounds().width - 10, getBounds().height - 10);

			fill(graph.x, graph.y, (int) (graph.x + graph.width * (0.9)), graph.y + graph.height,
				healthbarColor.get().toInt());
			fill((int) (graph.x + graph.width * (0.9)), graph.y, graph.x + graph.width, graph.y + graph.height,
				backgroundBarColor.get().toInt());
		}

		if (showText.get()) {
			String mem = "18/20";
			drawString(mem, pos.x + justification.get().getXOffset(client.textRenderer.getWidth(mem), getWidth() - 4) - 22,
				pos.y + (Math.round((float) height / 2) - 9) - (showAllocated.get() ? 4 : 0), ClientColors.WHITE,
				shadow.get());
			if (showAllocated.get()) {
				String alloc = I18n.translate("18") + ": 20";
				drawString(alloc, pos.x + justification.get().getXOffset(client.textRenderer.getWidth(alloc), getWidth() - 4) + 2,
					pos.y + (Math.round((float) height / 2) - 4) + 4, textColor.get(), shadow.get());
			}
		}

		if (!showBar.get() && !showText.get()) {
			String value = I18n.translate(ID.getPath());
			drawString(value, pos.x + justification.get().getXOffset(client.textRenderer.getWidth(value), getWidth() - 4) + 2,
				pos.y + (Math.round((float) height / 2) - 4), ClientColors.WHITE,
				shadow.get());
		}
	}

	@Override
	public boolean movable() {
		return true;
	}

	private float getUsage() {
		int max = (int) client.player.getMaxHealth(); // max
		int current = (int) client.player.getHealth(); // current

		return (float) current / max; // return as float used / max percentage
	}

	private String getMemoryLine() {
		int currentHealth = (int) client.player.getHealth();
		int maxHealth = (int) client.player.getMaxHealth();

		float absorption = client.player.getAbsorption();
		float totalHealth = currentHealth + absorption;

		String formattedHealth = (totalHealth % 1 == 0) ? String.format("%.0f", totalHealth) : String.format("%.1f", totalHealth);

		return formattedHealth + "/" + maxHealth;
	}

	@Override
	public List<Option<?>> getConfigurationOptions() {
		List<Option<?>> options = super.getConfigurationOptions();
		options.add(justification);
		options.add(anchor);
		options.add(showBar);
		options.add(showText);
		options.add(healthbarColor);
		options.add(backgroundBarColor);
		options.add(breathColor);
		return options;
	}

	@Override
	public Identifier getId() {
		return ID;
	}

	@Override
	public AnchorPoint getAnchor() {
		return anchor.get();
	}
}
