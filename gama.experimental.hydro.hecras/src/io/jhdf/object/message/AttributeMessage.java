/*
 * This file is part of jHDF. A pure Java library for accessing HDF5 files.
 *
 * http://jhdf.io
 *
 * Copyright 2019 James Mudd
 *
 * MIT License see 'LICENSE' file
 */
package io.jhdf.object.message;

import io.jhdf.Superblock;
import io.jhdf.Utils;
import io.jhdf.exceptions.UnsupportedHdfException;
import io.jhdf.object.datatype.DataType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.BitSet;

public class AttributeMessage extends Message {
	private static final Logger logger = LoggerFactory.getLogger(AttributeMessage.class);

	private static final int DATA_TYPE_SHARED = 0;
	private static final int DATA_SPACE_SHARED = 0;

	private final byte version;
	private final String name;
	private final DataType dataType;
	private final DataSpace dataSpace;
	private final ByteBuffer data;

	public AttributeMessage(ByteBuffer bb, Superblock sb, BitSet messageFlags) {
		super(messageFlags);

		version = bb.get();
		logger.trace("Version: {}", version);

		if (version == 1) {
			// Reserved byte
			bb.position(bb.position() + 1);

			final int nameSize = Utils.readBytesAsUnsignedInt(bb, 2);
			final int dataTypeSize = Utils.readBytesAsUnsignedInt(bb, 2);
			final int dataSpaceSize = Utils.readBytesAsUnsignedInt(bb, 2);

			name = Utils.readUntilNull(Utils.createSubBuffer(bb, nameSize));
			logger.trace("Name: {}", name);
			Utils.seekBufferToNextMultipleOfEight(bb);

			dataType = DataType.readDataType(Utils.createSubBuffer(bb, dataTypeSize));
			logger.trace("Datatype: {}", dataType);
			Utils.seekBufferToNextMultipleOfEight(bb);

			dataSpace = DataSpace.readDataSpace(Utils.createSubBuffer(bb, dataSpaceSize), sb);
			logger.trace("Dataspace: {}", dataSpace);
			Utils.seekBufferToNextMultipleOfEight(bb);

			final int dataSize = Math.toIntExact(dataSpace.getTotalLength() * dataType.getSize());
			if (dataSize == 0) {
				data = null;
			} else {
				data = Utils.createSubBuffer(bb, dataSize); // Create a new buffer starting at the current pos
			}

		} else if (version == 2) {
			final BitSet flags = BitSet.valueOf(new byte[] { bb.get() });

			final int nameSize = Utils.readBytesAsUnsignedInt(bb, 2);
			final int dataTypeSize = Utils.readBytesAsUnsignedInt(bb, 2);
			final int dataSpaceSize = Utils.readBytesAsUnsignedInt(bb, 2);

			name = Utils.readUntilNull(Utils.createSubBuffer(bb, nameSize));
			logger.trace("Name: {}", name);

			if (flags.get(DATA_TYPE_SHARED)) {
				throw new UnsupportedHdfException("Attribute contains shared data type");
			} else {
				dataType = DataType.readDataType(Utils.createSubBuffer(bb, dataTypeSize));
				logger.trace("Datatype: {}", dataType);
			}

			if (flags.get(DATA_SPACE_SHARED)) {
				throw new UnsupportedHdfException("Attribute contains shared data space");
			} else {
				dataSpace = DataSpace.readDataSpace(Utils.createSubBuffer(bb, dataSpaceSize), sb);
				logger.trace("Dataspace: {}", dataSpace);
			}

			final int dataSize = Math.toIntExact(dataSpace.getTotalLength() * dataType.getSize());
			if (dataSize == 0) {
				data = null;
			} else {
				data = Utils.createSubBuffer(bb, dataSize); // Create a new buffer starting at the current pos
			}

		} else if (version == 3) {
			final BitSet flags = BitSet.valueOf(new byte[] { bb.get() });

			final int nameSize = Utils.readBytesAsUnsignedInt(bb, 2);
			final int dataTypeSize = Utils.readBytesAsUnsignedInt(bb, 2);
			final int dataSpaceSize = Utils.readBytesAsUnsignedInt(bb, 2);

			final byte characterEncoding = bb.get();
			final Charset charset;
			switch (characterEncoding) {
			case 0:
				charset = StandardCharsets.US_ASCII;
				break;
			case 1:
				charset = StandardCharsets.UTF_8;
				break;
			default:
				throw new UnsupportedHdfException("Unrecognized character set detected: " + characterEncoding);
			}

			ByteBuffer nameBuffer = Utils.createSubBuffer(bb, nameSize);
			name = charset.decode(nameBuffer).toString().trim();
			logger.trace("Name: {}", name);

			if (flags.get(DATA_TYPE_SHARED)) {
				throw new UnsupportedHdfException("Attribute contains shared data type");
			} else {
				dataType = DataType.readDataType(Utils.createSubBuffer(bb, dataTypeSize));
				logger.trace("Datatype: {}", dataType);
			}

			if (flags.get(DATA_SPACE_SHARED)) {
				throw new UnsupportedHdfException("Attribute contains shared data space");
			} else {
				dataSpace = DataSpace.readDataSpace(Utils.createSubBuffer(bb, dataSpaceSize), sb);
				logger.trace("Dataspace: {}", dataSpace);
			}

			final int dataSize = Math.toIntExact(dataSpace.getTotalLength() * dataType.getSize());
			if (dataSize == 0) {
				data = null;
			} else {
				data = Utils.createSubBuffer(bb, dataSize); // Create a new buffer starting at the current pos
			}

		} else {
			throw new UnsupportedHdfException("Unsupported Attribute message version. Detected version: " + version);
		}

		logger.debug("Read attribute: {}", name);
	}

	public int getVersion() {
		return version;
	}

	public String getName() {
		return name;
	}

	public DataType getDataType() {
		return dataType;
	}

	public DataSpace getDataSpace() {
		return dataSpace;
	}

	public ByteBuffer getDataBuffer() {
		if (data == null) {
			return null;
		} else {
			// Slice the buffer to allow multiple accesses
			return data.slice().order(data.order());
		}
	}

	@Override
	public String toString() {
		return "AttributeMessage [name=" + name + ", dataType=" + dataType + ", dataSpace=" + dataSpace + "]";
	}

}
