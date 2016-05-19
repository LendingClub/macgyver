package io.macgyver.cli.command;

import java.io.File;
import java.io.IOException;
import org.assertj.core.api.Assertions;
import org.assertj.core.util.Files;
import org.junit.Test;

import com.beust.jcommander.ParameterException;

import io.macgyver.cli.CLI;

public class HelpCommandTest {

	@Test
	public void helpTest() throws IOException {
		CLI cli = new CLI();

		File configDir = Files.newTemporaryFolder();

		cli.getConfigManager().setConfigDir(configDir);

		try {
			cli.run("config", "--help");
		} catch (Exception e) {
			Assertions.fail("Failed to run the help command.");
		}

	}

	@Test
	public void helpTestNegative() throws IOException {
		CLI cli = new CLI();

		File configDir = Files.newTemporaryFolder();

		cli.getConfigManager().setConfigDir(configDir);
		try {
			cli.run("config", "--test");
		} catch (Exception e) {
			Assertions.assertThat(e).isInstanceOf(ParameterException.class)
			.hasMessage("Unknown option: --test");
		}

	}
}
