package net.hirschauer.yaas.lighthouse.util;

import java.util.Properties;

public interface IStorable {
	void store(Properties values);
	void load(Properties values);
}
