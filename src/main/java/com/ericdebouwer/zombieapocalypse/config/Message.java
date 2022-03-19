package com.ericdebouwer.zombieapocalypse.config;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public enum Message {
	START_SUCCESS("started-success"),
	START_BROADCAST ("started-broadcast"),
	START_FAIL ("start-failed"),
	START_INVALID_INT("start-invalid-duration"),
	END_SUCCESS ("ended-success"),
	END_FAIL ("end-failed"),
	END_BROADCAST ("ended-broadcast"),
	NO_WORLD ("console-no-world-provided"),
	INVALID_WORLD ("invalid-world"),
	NO_PERMISSION ("no-command-permission"),
	RELOAD_SUCCESS ("reload-success"),
	RELOAD_FAIL("reload-fail"),
	ITEM_GIVEN ("given-zombie-egg"),
	INVALID_ZOMBIE ("invalid-egg-type"),
	BOSS_BAR_TITLE ("apocalypse-boss-bar-title"),
	NO_SLEEP ("no-apocalypse-sleep"),
	CAP_NO_ARGS ("mobcap-too-few-arguments"),
	CAP_INVALID_AMOUNT ("mobcap-invalid-amount"),
	CAP_NO_APOCALYPSE("mobcap-no-apocalypse"),
	CAP_SUCCESS ("mobcap-success");
	
	@Getter
	private final String key;
}
