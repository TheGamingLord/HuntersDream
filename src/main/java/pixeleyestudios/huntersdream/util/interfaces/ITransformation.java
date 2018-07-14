package pixeleyestudios.huntersdream.util.interfaces;

import pixeleyestudios.huntersdream.util.helpers.TransformationHelper;
import pixeleyestudios.huntersdream.util.helpers.TransformationHelper.Transformations;

/**
 * This interface is for entites that can transform
 */
public interface ITransformation {

	public boolean transformed();

	public void setTransformed(boolean transformed);

	public int getTransformationInt();

	default public Transformations getTransformation() {
		return TransformationHelper.Transformations.fromID(getTransformationInt());
	}

	public void setTransformationID(int id);

	default public void setTransformation(TransformationHelper.Transformations transformation) {
		setTransformationID(transformation.ID);
	}

	public int getTextureIndex();
}
