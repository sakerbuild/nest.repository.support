package saker.nest.support.api.server.upload;

import saker.build.thirdparty.saker.util.StringUtils;
import saker.nest.bundle.BundleIdentifier;

/**
 * Represents the result of a bundle uploading worker task.
 * <p>
 * The interface provides access to information about the uploaded bundle.
 * <p>
 * Clients shouldn't implement this interface.
 * 
 * @see BundleUploadTaskOutput
 */
public interface BundleUploadWorkerTaskOutput {
	/**
	 * Gets the identifier of the uploaded bundle.
	 * 
	 * @return The identifier.
	 */
	public BundleIdentifier getBundleIdentifier();

	//hexa encoded
	/**
	 * Gets the <code>MD5</code> hash of the bundle contents that was uploaded.
	 * 
	 * @return The <code>MD5</code> hash in a hexa encoded format.
	 * @see StringUtils#toHexString(byte[])
	 * @see StringUtils#parseHexString(CharSequence)
	 */
	public String getMD5();

	//hexa encoded
	/**
	 * Gets the <code>SHA256</code> hash of the bundle contents that was uploaded.
	 * 
	 * @return The <code>SHA256</code> hash in a hexa encoded format.
	 * @see StringUtils#toHexString(byte[])
	 * @see StringUtils#parseHexString(CharSequence)
	 */
	public String getSHA256();
}
