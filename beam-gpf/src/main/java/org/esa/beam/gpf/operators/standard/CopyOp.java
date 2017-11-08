package org.esa.beam.gpf.operators.standard;

/**
 * Created by knowles on 11/8/17.
 */

import org.esa.beam.framework.datamodel.Product;
import org.esa.beam.framework.gpf.*;
import org.esa.beam.framework.gpf.annotations.OperatorMetadata;
import org.esa.beam.framework.gpf.annotations.SourceProduct;
import org.esa.beam.framework.gpf.annotations.TargetProduct;

import org.esa.beam.framework.datamodel.Product;
import org.esa.beam.framework.gpf.Operator;
import org.esa.beam.framework.gpf.OperatorException;
import org.esa.beam.framework.gpf.OperatorSpi;
import org.esa.beam.framework.gpf.annotations.OperatorMetadata;
import org.esa.beam.framework.gpf.annotations.SourceProduct;
import org.esa.beam.framework.gpf.annotations.TargetProduct;

import java.util.HashMap;

@OperatorMetadata(alias = "Copy",
        description = "Creates target product which is an exact copy of the source product.",
        internal = true)
public class CopyOp extends Operator {
    @SourceProduct
    private Product sourceProduct;

    @TargetProduct
    private Product targetProduct;

    public CopyOp() {
    }

    public CopyOp(Product sourceProduct) {
        this.sourceProduct = sourceProduct;
        this.targetProduct = sourceProduct;
    }

    @Override
    public void initialize() throws OperatorException {
        if (sourceProduct == null) {
            throw new OperatorException("No source product given.");
        }

        if (sourceProduct.getSceneRasterWidth() == 0 || sourceProduct.getSceneRasterHeight() == 0 ) {
            throw new OperatorException("Source product has no dimension.");
        }

        copySourceToTarget();
    }

    private void copySourceToTarget() {
        final HashMap<String, Object> subsetParameters = new HashMap<String, Object>();
        subsetParameters.put("x", 0);
        subsetParameters.put("y", 0);
        subsetParameters.put("width", sourceProduct.getSceneRasterWidth());
        subsetParameters.put("height", sourceProduct.getSceneRasterHeight());

        HashMap<String, Product> projProducts = new HashMap<String, Product>();
        projProducts.put("source", sourceProduct);
        targetProduct = GPF.createProduct("Subset", subsetParameters, projProducts);
    }

    public static class Spi extends OperatorSpi {
        public Spi() {
            super(CopyOp.class);
        }
    }
}
