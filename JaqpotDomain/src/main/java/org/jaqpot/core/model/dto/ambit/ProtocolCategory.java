/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jaqpot.core.model.dto.ambit;

/**
 *
 * @author hampos
 */
public enum ProtocolCategory {

    /**
     * Phys chem properties
     */
    GI_GENERAL_INFORM_SECTION("P-CHEM") {
                @Override
                public String toString() {
                    return "Appearance";
                }
            },
    PC_MELTING_SECTION("P-CHEM") {
                @Override
                public String toString() {
                    return "Melting point / freezing point";
                }
            },
    PC_BOILING_SECTION("P-CHEM") {
                @Override
                public String toString() {
                    return "Boiling point";
                }
            },
    PC_GRANULOMETRY_SECTION("P-CHEM") {
                @Override
                public String toString() {
                    return "Particle size distribution (Granulometry)";
                }
            },
    PC_VAPOUR_SECTION("P-CHEM") {
                @Override
                public String toString() {
                    return "Vapour pressure";
                }
            },
    PC_PARTITION_SECTION("P-CHEM") {
                @Override
                public String toString() {
                    return "Partition coefficient";
                }
            },
    PC_WATER_SOL_SECTION("P-CHEM") {
                @Override
                public String toString() {
                    return "Water solubility";
                }
            },
    PC_SOL_ORGANIC_SECTION("P-CHEM") {
                @Override
                public String toString() {
                    return "Solubility in organic solvents";
                }
            },
    PC_NON_SATURATED_PH_SECTION("P-CHEM") {
                @Override
                public String toString() {
                    return "pH";
                }
            },
    PC_DISSOCIATION_SECTION("P-CHEM") {
                @Override
                public String toString() {
                    return "Dissociation constant";
                }
            },
    PC_UNKNOWN_SECTION("P-CHEM") {
                @Override
                public String toString() {
                    return "Physico chemical properties (other)";
                }

            },
    /**
     * Environmental fate
     */
    TO_PHOTOTRANS_AIR_SECTION("ENV FATE") {
                @Override
                public String toString() {
                    return "Phototransformation in Air";
                }

            },
    TO_HYDROLYSIS_SECTION("ENV FATE") {
                @Override
                public String toString() {
                    return "Hydrolysis";
                }

            },
    TO_BIODEG_WATER_SCREEN_SECTION("ENV FATE") {
                @Override
                public String toString() {
                    return "Biodegradation in water - screening tests";
                }

            },
    TO_BIODEG_WATER_SIM_SECTION("ENV FATE") {
                @Override
                public String toString() {
                    return "Biodegradation in water and sediment: simulation tests";
                }

            },
    EN_STABILITY_IN_SOIL_SECTION("ENV FATE") {
                @Override
                public String toString() {
                    return "Biodegradation in Soil";
                }

            },
    EN_BIOACCUMULATION_SECTION("ENV FATE") {
                @Override
                public String toString() {
                    return "Bioaccumulation: aquatic / sediment";
                }

            },
    EN_BIOACCU_TERR_SECTION("ENV FATE") {
                @Override
                public String toString() {
                    return "Bioaccumulation: terrestrial";
                }

            },
    EN_ADSORPTION_SECTION("ENV FATE") {
                @Override
                public String toString() {
                    return "Adsorption / Desorption";
                }

            },
    EN_HENRY_LAW_SECTION("ENV FATE") {
                @Override
                public String toString() {
                    return "Henry's Law constant";
                }

            },
    /**
     * Toxicity
     */
    TO_ACUTE_ORAL_SECTION("TOX") {
                @Override
                public String toString() {
                    return "Acute toxicity - oral";
                }

            },
    TO_ACUTE_INHAL_SECTION("TOX") {
                @Override
                public String toString() {
                    return "Acute toxicity - inhalation";
                }

            },
    TO_ACUTE_DERMAL_SECTION("TOX") {
                @Override
                public String toString() {
                    return "Acute toxicity - dermal";
                }

            },
    TO_SKIN_IRRITATION_SECTION("TOX") {
                @Override
                public String toString() {
                    return "Skin irritation / Corrosion";
                }

            },
    TO_EYE_IRRITATION_SECTION("TOX") {
                @Override
                public String toString() {
                    return "Eye irritation";
                }

            },
    TO_SENSITIZATION_SECTION("TOX") {
                @Override
                public String toString() {
                    return "Skin sensitisation";
                }

            },
    TO_SENSITIZATION_HUMAN_SECTION("TOX") {
                @Override
                public String toString() {
                    return "Skin sensitisation (human)";
                }

            },
    TO_SENSITIZATION_INVITRO_SECTION("TOX") {
                @Override
                public String toString() {
                    return "Skin sensitisation (in vitro)";
                }

            },
    TO_SENSITIZATION_INCHEMICO_SECTION("TOX") {
                @Override
                public String toString() {
                    return "Skin sensitisation (in chemico)";
                }

            },
    TO_REPEATED_ORAL_SECTION("TOX") {
                @Override
                public String toString() {
                    return "Repeated dose toxicity - oral";
                }

            },
    TO_REPEATED_INHAL_SECTION("TOX") {
                @Override
                public String toString() {
                    return "Repeated dose toxicity - inhalation";
                }

            },
    TO_REPEATED_DERMAL_SECTION("TOX") {
                @Override
                public String toString() {
                    return "Repeated dose toxicity - dermal";
                }

            },
    TO_GENETIC_IN_VITRO_SECTION("TOX") {
                @Override
                public String toString() {
                    return "Genetic toxicity in vitro";
                }

            },
    TO_GENETIC_IN_VIVO_SECTION("TOX") {
                @Override
                public String toString() {
                    return "Genetic toxicity in vivo";
                }

            },
    TO_CARCINOGENICITY_SECTION("TOX") {
                @Override
                public String toString() {
                    return "Carcinogenicity";
                }

            },
    TO_REPRODUCTION_SECTION("TOX") {
                @Override
                public String toString() {
                    return "Toxicity to reproduction";
                }

            },
    TO_DEVELOPMENTAL_SECTION("TOX") {
                @Override
                public String toString() {
                    return "Developmental toxicity / teratogenicity";
                }

            },
    /**
     * Ecotoxicity
     */
    EC_FISHTOX_SECTION("ECOTOX") {
                @Override
                public String toString() {
                    return "Short-term toxicity to fish";
                }

            },
    EC_CHRONFISHTOX_SECTION("ECOTOX") {
                @Override
                public String toString() {
                    return "Long-term toxicity to fish";
                }

            },
    EC_DAPHNIATOX_SECTION("ECOTOX") {
                @Override
                public String toString() {
                    return "Short-term toxicity to aquatic inverterbrates";
                }

            },
    EC_CHRONDAPHNIATOX_SECTION("ECOTOX") {
                @Override
                public String toString() {
                    return "Long-term toxicity to aquatic inverterbrates";
                }

            },
    EC_ALGAETOX_SECTION("ECOTOX") {
                @Override
                public String toString() {
                    return "Toxicity to aquatic algae and cyanobacteria";
                }

            },
    EC_BACTOX_SECTION("ECOTOX") {
                @Override
                public String toString() {
                    return "Toxicity to microorganisms";
                }

            },
    EC_SEDIMENTDWELLINGTOX_SECTION("ECOTOX") {
                @Override
                public String toString() {
                    return "Sediment toxicity";
                }

            },
    EC_SOILDWELLINGTOX_SECTION("ECOTOX") {
                @Override
                public String toString() {
                    return "Toxicity to soil macroorganisms";
                }

            },
    EC_HONEYBEESTOX_SECTION("ECOTOX") {
                @Override
                public String toString() {
                    return "Toxicity to terrestrial arthropods";
                }

            },
    EC_PLANTTOX_SECTION("ECOTOX") {
                @Override
                public String toString() {
                    return "Toxicity to terrestrial plants";
                }

            },
    EC_SOIL_MICRO_TOX_SECTION("ECOTOX") {
                @Override
                public String toString() {
                    return "Toxicity to soil microorganisms";
                }

            },
    AGGLOMERATION_AGGREGATION_SECTION("P-CHEM") {
                @Override
                public String toString() {
                    return "Nanomaterial agglomeration/aggregation";
                }

            },
    CRYSTALLINE_PHASE_SECTION("P-CHEM") {
                @Override
                public String toString() {
                    return "Nanomaterial crystalline phase";
                }

            },
    CRYSTALLITE_AND_GRAIN_SIZE_SECTION("P-CHEM") {
                @Override
                public String toString() {
                    return "Nanomaterial crystallite and grain size";
                }

            },
    ASPECT_RATIO_SHAPE_SECTION("P-CHEM") {
                @Override
                public String toString() {
                    return "Nanomaterial aspect ratio/shape";
                }

            },
    SPECIFIC_SURFACE_AREA_SECTION("P-CHEM") {
                @Override
                public String toString() {
                    return "Nanomaterial specific surface area";
                }

            },
    ZETA_POTENTIAL_SECTION("P-CHEM") {
                @Override
                public String toString() {
                    return "Nanomaterial zeta potential";
                }

            },
    SURFACE_CHEMISTRY_SECTION("P-CHEM") {
                @Override
                public String toString() {
                    return "Nanomaterial surface chemistry";
                }

            },
    DUSTINESS_SECTION("P-CHEM") {
                @Override
                public String toString() {
                    return "Nanomaterial dustiness";
                }

            },
    POROSITY_SECTION("P-CHEM") {
                @Override
                public String toString() {
                    return "Nanomaterial porosity";
                }

            },
    POUR_DENSITY_SECTION("P-CHEM") {
                @Override
                public String toString() {
                    return "Nanomaterial pour density";
                }

            },
    PHOTOCATALYTIC_ACTIVITY_SECTION("P-CHEM") {
                @Override
                public String toString() {
                    return "Nanomaterial photocatalytic activity";
                }

            },
    CATALYTIC_ACTIVITY_SECTION("P-CHEM") {
                @Override
                public String toString() {
                    return "Nanomaterial catalytic activity";
                }

            },
    UNKNOWN_TOXICITY_SECTION("TOX") {
                @Override
                public String toString() {
                    return "Unclassified toxicity";
                }

            },
    PUBCHEM_CONFIRMATORY_SECTION("TOX"),
    PUBCHEM_SUMMARY_SECTION("TOX"),
    PUBCHEM_SCREENING_SECTION("TOX"),
    PUBCHEM_DOSERESPONSE_SECTION("TOX"),
    PUBCHEM_PANEL_SECTION("TOX"),
    PROTEOMICS_SECTION("TOX") {
                @Override
                public String toString() {
                    return "Proteomics";
                }

            },
    BAO_0003009_SECTION("TOX") {
                @Override
                public String toString() {
                    return "Cell Viability Assay";
                }

            },
    BAO_0002993_SECTION("TOX") {
                @Override
                public String toString() {
                    return "Cytotoxicity Assay";
                }

            },
    BAO_0002100_SECTION("TOX") {
                @Override
                public String toString() {
                    return "Cell Growth Assay";
                }

            },
    BAO_0002167_SECTION("TOX") {
                // to be merged with genotox
                @Override
                public String toString() {
                    return "Genotoxicity Assay";
                }

            },
    BAO_0002168_SECTION("TOX") {
                // to be merged with I5 category
                @Override
                public String toString() {
                    return "Oxidative Stress Assay";
                }

            },
    BAO_0002189_SECTION("TOX") {
                // to be merged with I5 category
                @Override
                public String toString() {
                    return "Toxicity Assay";
                }

            },
    // Toxcast - BAO
    CELL_CYCLE("TOX"),
    CELL_DEATH("TOX"),
    CELL_MORPHOLOGY("TOX"),
    CELL_PROLIFERATION("TOX"),
    MITOCHONDRIAL_DEPOLARIZATION("TOX"),
    OXIDATIVE_PHOSPHORYLATION("TOX"),
    PROTEIN_STABILIZATION("TOX"),
    RECEPTOR_BINDING("TOX"),
    REGULATION_OF_CATALYTIC_ACTIVITY("TOX"),
    REGULATION_OF_GENE_EXPRESSION("TOX"),
    REGULATION_OF_TRANSCRIPTION_FACTOR_ACTIVITY("TOX"),
    NA("TOX");

    private final String topCategory;

    ProtocolCategory(String topCategory) {
        this.topCategory = topCategory;
    }

    public String getTopCategory() {
        return topCategory;
    }

}
