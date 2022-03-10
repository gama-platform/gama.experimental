package ummisco.gama.unity.client.messages;

// The map elements
public class ItemAttributes {
	public Object attribute;
	public Object value;

	public ItemAttributes() {

	}

	public ItemAttributes(Object attributeName, Object attributeValue) {
		this.attribute = attributeName;
		this.value = attributeValue;
	}
}
