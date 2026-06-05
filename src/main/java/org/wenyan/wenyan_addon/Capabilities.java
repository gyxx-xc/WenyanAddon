package org.wenyan.wenyan_addon;

import indi.wenyan.content.block.runner.BlockRequest;
import indi.wenyan.interpreter_impl.HandlerPackageBuilder;
import indi.wenyan.interpreter_impl.IWenyanBlockDevice;
import indi.wenyan.judou.api.WenyanException;
import indi.wenyan.judou.api.exec.IRequestCallHandler;
import indi.wenyan.judou.api.exec.request.IArgsRequest;
import indi.wenyan.judou.api.exec.request.IBaseHandleableRequest;
import indi.wenyan.judou.api.exec.structure.IHandleContext;
import indi.wenyan.judou.api.exec.structure.RawHandlerPackage;
import indi.wenyan.judou.api.runtime.IWenyanRunner;
import indi.wenyan.judou.api.utils.ChineseUtils;
import indi.wenyan.judou.api.values.IWenyanValue;
import indi.wenyan.judou.api.values.WenyanPackage;
import indi.wenyan.judou.api.values.WenyanNull;
import indi.wenyan.judou.api.values.primitive.WenyanDouble;
import indi.wenyan.judou.api.values.primitive.WenyanString;
import indi.wenyan.setup.definitions.WyRegistration;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.sheep.Sheep;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.projectile.FireworkRocketEntity;
import net.minecraft.world.entity.projectile.arrow.Arrow;
import net.minecraft.world.entity.projectile.hurtingprojectile.SmallFireball;
import net.minecraft.world.entity.projectile.throwableitemprojectile.Snowball;
import net.minecraft.world.item.BoneMealItem;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.WrittenBookContent;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.LecternBlockEntity;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.capabilities.IBlockCapabilityProvider;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import org.jspecify.annotations.NonNull;

import java.util.HashMap;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

public enum Capabilities {
    ;

    private static final RawHandlerPackage DYE_PACKAGE = dyePackage();
    private static boolean dyeGlobalsLogged;

    public static void injectDyeGlobals(WenyanPackage environment) {
        DYE_PACKAGE.functions().forEach((name, request) -> {
            IRequestCallHandler handler = contextualHandler(request);
            environment.put(name, handler);
            environment.put(ChineseUtils.toSimplifiedVar(name), handler);
        });
        if (!dyeGlobalsLogged) {
            dyeGlobalsLogged = true;
            WenyanAddon.LOGGER.info(
                    "Injected {} dye globals into Wenyan runner environment; has 染前浅蓝 = {}",
                    DYE_PACKAGE.functions().size(),
                    environment.variables().containsKey(ChineseUtils.bracketOf("染前浅蓝"))
            );
        }
    }

    private static IRequestCallHandler contextualHandler(Supplier<RawHandlerPackage.IRawRequest> request) {
        return (thread, self, args, onReturn) -> new ContextualRequest(thread, self, args, request.get(), onReturn);
    }

    public static void registerCapabilities(@NonNull RegisterCapabilitiesEvent event) {
        event.registerBlock(
                WyRegistration.WENYAN_BLOCK_DEVICE_CAPABILITY,
                simpleDevice("crush game", HandlerPackageBuilder.create()
                        .handler(ChineseUtils.bracketOf("crush"),
                                _ -> {
                                    throw new NullPointerException();
                                })
                        .build()),
                WenyanAddon.EXAMPLE_BLOCK.get(),
                Blocks.BEDROCK
        );

        event.registerBlock(
                WyRegistration.WENYAN_BLOCK_DEVICE_CAPABILITY,
                simpleDevice("projectile spawner", HandlerPackageBuilder.create()
                        .handler(ChineseUtils.bracketOf("箭"), (iHandleContext, iArgsRequest) -> {
                            if (iHandleContext instanceof BlockRequest.BlockContext context) {
                                double x = iArgsRequest.args().get(0).as(WenyanDouble.TYPE).value() + context.pos().getX();
                                double y = iArgsRequest.args().get(1).as(WenyanDouble.TYPE).value() + context.pos().getY();
                                double z = iArgsRequest.args().get(2).as(WenyanDouble.TYPE).value() + context.pos().getZ();
                                Arrow arrow = new Arrow(context.level(), x, y, z, ItemStack.EMPTY, null);
                                arrow.shoot(0, 1, 0, 0.6f, 10.0f);
                                context.level().addFreshEntity(arrow);
                            }
                            return WenyanNull.NULL;
                        })
                        .handler(ChineseUtils.bracketOf("焰火"), (iHandleContext, iArgsRequest) -> {
                            if (iHandleContext instanceof BlockRequest.BlockContext context) {
                                double x = iArgsRequest.args().get(0).as(WenyanDouble.TYPE).value() + context.pos().getX();
                                double y = iArgsRequest.args().get(1).as(WenyanDouble.TYPE).value() + context.pos().getY();
                                double z = iArgsRequest.args().get(2).as(WenyanDouble.TYPE).value() + context.pos().getZ();
                                FireworkRocketEntity firework = new FireworkRocketEntity(context.level(), x, y, z,
                                        new ItemStack(Items.FIREWORK_ROCKET));
                                firework.shoot(0, 1, 0, 0.6f, 10.0f);
                                context.level().addFreshEntity(firework);
                            }
                            return WenyanNull.NULL;
                        })
                        .handler(ChineseUtils.bracketOf("雪球"), (iHandleContext, iArgsRequest) -> {
                            if (iHandleContext instanceof BlockRequest.BlockContext context) {
                                double x = iArgsRequest.args().get(0).as(WenyanDouble.TYPE).value() + context.pos().getX();
                                double y = iArgsRequest.args().get(1).as(WenyanDouble.TYPE).value() + context.pos().getY();
                                double z = iArgsRequest.args().get(2).as(WenyanDouble.TYPE).value() + context.pos().getZ();
                                Snowball snowball = new Snowball(context.level(), x, y, z, ItemStack.EMPTY);
                                snowball.shoot(0, 1, 0, 0.6f, 10.0f);
                                context.level().addFreshEntity(snowball);
                            }
                            return WenyanNull.NULL;
                        })
                        .handler(ChineseUtils.bracketOf("小火球"), (iHandleContext, iArgsRequest) -> {
                            if (iHandleContext instanceof BlockRequest.BlockContext context) {
                                double x = iArgsRequest.args().get(0).as(WenyanDouble.TYPE).value() + context.pos().getX();
                                double y = iArgsRequest.args().get(1).as(WenyanDouble.TYPE).value() + context.pos().getY();
                                double z = iArgsRequest.args().get(2).as(WenyanDouble.TYPE).value() + context.pos().getZ();
                                SmallFireball fireball = new SmallFireball(context.level(), x, y, z,
                                        new Vec3(0, 1, 0));
                                context.level().addFreshEntity(fireball);
                            }
                            return WenyanNull.NULL;
                        })
                        .build()),
                WenyanAddon.PROJECTILE_SPAWNER_BLOCK.get(),
                Blocks.DISPENSER
        );

        event.registerBlock(
                WyRegistration.WENYAN_BLOCK_DEVICE_CAPABILITY,
                simpleDevice("elemental", HandlerPackageBuilder.create()
                        .handler(ChineseUtils.bracketOf("水源"), (iHandleContext, iArgsRequest) -> {
                            if (iHandleContext instanceof BlockRequest.BlockContext context) {
                                double x = iArgsRequest.args().get(0).as(WenyanDouble.TYPE).value() + context.pos().getX();
                                double y = iArgsRequest.args().get(1).as(WenyanDouble.TYPE).value() + context.pos().getY();
                                double z = iArgsRequest.args().get(2).as(WenyanDouble.TYPE).value() + context.pos().getZ();
                                BlockPos pos = new BlockPos((int) x, (int) y, (int) z);
                                context.level().setBlock(pos, Blocks.WATER.defaultBlockState(), 3);
                            }
                            return WenyanNull.NULL;
                        })
                        .handler(ChineseUtils.bracketOf("岩浆"), (iHandleContext, iArgsRequest) -> {
                            if (iHandleContext instanceof BlockRequest.BlockContext context) {
                                double x = iArgsRequest.args().get(0).as(WenyanDouble.TYPE).value() + context.pos().getX();
                                double y = iArgsRequest.args().get(1).as(WenyanDouble.TYPE).value() + context.pos().getY();
                                double z = iArgsRequest.args().get(2).as(WenyanDouble.TYPE).value() + context.pos().getZ();
                                BlockPos pos = new BlockPos((int) x, (int) y, (int) z);
                                context.level().setBlock(pos, Blocks.LAVA.defaultBlockState(), 3);
                            }
                            return WenyanNull.NULL;
                        })
                        .handler(ChineseUtils.bracketOf("清除流体"), (iHandleContext, iArgsRequest) -> {
                            if (iHandleContext instanceof BlockRequest.BlockContext context) {
                                double x = iArgsRequest.args().get(0).as(WenyanDouble.TYPE).value() + context.pos().getX();
                                double y = iArgsRequest.args().get(1).as(WenyanDouble.TYPE).value() + context.pos().getY();
                                double z = iArgsRequest.args().get(2).as(WenyanDouble.TYPE).value() + context.pos().getZ();
                                BlockPos pos = new BlockPos((int) x, (int) y, (int) z);
                                BlockState state = context.level().getBlockState(pos);
                                if (!state.getFluidState().isEmpty()) {
                                    context.level().setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
                                }
                            }
                            return WenyanNull.NULL;
                        })
                        .handler(ChineseUtils.bracketOf("冻水成冰"), (iHandleContext, iArgsRequest) -> {
                            if (iHandleContext instanceof BlockRequest.BlockContext context) {
                                double x = iArgsRequest.args().get(0).as(WenyanDouble.TYPE).value() + context.pos().getX();
                                double y = iArgsRequest.args().get(1).as(WenyanDouble.TYPE).value() + context.pos().getY();
                                double z = iArgsRequest.args().get(2).as(WenyanDouble.TYPE).value() + context.pos().getZ();
                                BlockPos pos = new BlockPos((int) x, (int) y, (int) z);
                                if (context.level().getBlockState(pos).is(Blocks.WATER)) {
                                    context.level().setBlock(pos, Blocks.ICE.defaultBlockState(), 3);
                                }
                            }
                            return WenyanNull.NULL;
                        })
                        .build()),
                WenyanAddon.ELEMENTAL_BLOCK.get(),
                Blocks.CAULDRON
        );

        event.registerBlock(
                WyRegistration.WENYAN_BLOCK_DEVICE_CAPABILITY,
                simpleDevice("world interaction", HandlerPackageBuilder.create()
                        .handler(ChineseUtils.bracketOf("催生"), (iHandleContext, iArgsRequest) -> {
                            if (iHandleContext instanceof BlockRequest.BlockContext context) {
                                double x = iArgsRequest.args().get(0).as(WenyanDouble.TYPE).value() + context.pos().getX();
                                double y = iArgsRequest.args().get(1).as(WenyanDouble.TYPE).value() + context.pos().getY();
                                double z = iArgsRequest.args().get(2).as(WenyanDouble.TYPE).value() + context.pos().getZ();
                                BlockPos pos = new BlockPos((int) x, (int) y, (int) z);
                                BoneMealItem.applyBonemeal(ItemStack.EMPTY, context.level(), pos, null);
                            }
                            return WenyanNull.NULL;
                        })
                        .handler(ChineseUtils.bracketOf("点燃"), (iHandleContext, iArgsRequest) -> {
                            if (iHandleContext instanceof BlockRequest.BlockContext context) {
                                double x = iArgsRequest.args().get(0).as(WenyanDouble.TYPE).value() + context.pos().getX();
                                double y = iArgsRequest.args().get(1).as(WenyanDouble.TYPE).value() + context.pos().getY();
                                double z = iArgsRequest.args().get(2).as(WenyanDouble.TYPE).value() + context.pos().getZ();
                                BlockPos pos = new BlockPos((int) x, (int) y, (int) z);
                                BlockPos abovePos = pos.above();
                                if (context.level().getBlockState(abovePos).isAir()) {
                                    context.level().setBlock(abovePos, Blocks.FIRE.defaultBlockState(), 3);
                                }
                            }
                            return WenyanNull.NULL;
                        })
                        .handler(ChineseUtils.bracketOf("熄灭"), (iHandleContext, iArgsRequest) -> {
                            if (iHandleContext instanceof BlockRequest.BlockContext context) {
                                double x = iArgsRequest.args().get(0).as(WenyanDouble.TYPE).value() + context.pos().getX();
                                double y = iArgsRequest.args().get(1).as(WenyanDouble.TYPE).value() + context.pos().getY();
                                double z = iArgsRequest.args().get(2).as(WenyanDouble.TYPE).value() + context.pos().getZ();
                                BlockPos pos = new BlockPos((int) x, (int) y, (int) z);
                                if (context.level().getBlockState(pos).is(Blocks.FIRE)) {
                                    context.level().setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
                                }
                                AABB area = new AABB(pos).inflate(3.0);
                                for (Entity entity : context.level().getEntities(null, area)) {
                                    entity.clearFire();
                                }
                            }
                            return WenyanNull.NULL;
                        })
                        .build()),
                WenyanAddon.WORLD_INTERACTION_BLOCK.get(),
                Blocks.COMPOSTER
        );

        event.registerBlock(
                WyRegistration.WENYAN_BLOCK_DEVICE_CAPABILITY,
                simpleDevice("entity manipulation", HandlerPackageBuilder.create()
                        .handler(ChineseUtils.bracketOf("传送"), (iHandleContext, iArgsRequest) -> {
                            if (iHandleContext instanceof BlockRequest.BlockContext context) {
                                double sx = iArgsRequest.args().get(0).as(WenyanDouble.TYPE).value() + context.pos().getX();
                                double sy = iArgsRequest.args().get(1).as(WenyanDouble.TYPE).value() + context.pos().getY();
                                double sz = iArgsRequest.args().get(2).as(WenyanDouble.TYPE).value() + context.pos().getZ();
                                double dx = iArgsRequest.args().get(3).as(WenyanDouble.TYPE).value() + context.pos().getX();
                                double dy = iArgsRequest.args().get(4).as(WenyanDouble.TYPE).value() + context.pos().getY();
                                double dz = iArgsRequest.args().get(5).as(WenyanDouble.TYPE).value() + context.pos().getZ();
                                AABB area = new AABB(sx - 0.5, sy - 0.5, sz - 0.5, sx + 0.5, sy + 0.5, sz + 0.5);
                                for (Entity entity : context.level().getEntities(null, area)) {
                                    entity.teleportTo(dx, dy, dz);
                                }
                            }
                            return WenyanNull.NULL;
                        })
                        .handler(ChineseUtils.bracketOf("闪现"), (iHandleContext, iArgsRequest) -> {
                            if (iHandleContext instanceof BlockRequest.BlockContext context) {
                                double x = iArgsRequest.args().get(0).as(WenyanDouble.TYPE).value() + context.pos().getX();
                                double y = iArgsRequest.args().get(1).as(WenyanDouble.TYPE).value() + context.pos().getY();
                                double z = iArgsRequest.args().get(2).as(WenyanDouble.TYPE).value() + context.pos().getZ();
                                double dx = iArgsRequest.args().get(3).as(WenyanDouble.TYPE).value();
                                double dy = iArgsRequest.args().get(4).as(WenyanDouble.TYPE).value();
                                double dz = iArgsRequest.args().get(5).as(WenyanDouble.TYPE).value();
                                AABB area = new AABB(x - 0.5, y - 0.5, z - 0.5, x + 0.5, y + 0.5, z + 0.5);
                                for (Entity entity : context.level().getEntities(null, area)) {
                                    entity.teleportTo(x + dx, y + dy, z + dz);
                                }
                            }
                            return WenyanNull.NULL;
                        })
                        .handler(ChineseUtils.bracketOf("施力"), (iHandleContext, iArgsRequest) -> {
                            if (iHandleContext instanceof BlockRequest.BlockContext context) {
                                double x = iArgsRequest.args().get(0).as(WenyanDouble.TYPE).value() + context.pos().getX();
                                double y = iArgsRequest.args().get(1).as(WenyanDouble.TYPE).value() + context.pos().getY();
                                double z = iArgsRequest.args().get(2).as(WenyanDouble.TYPE).value() + context.pos().getZ();
                                double fx = iArgsRequest.args().get(3).as(WenyanDouble.TYPE).value();
                                double fy = iArgsRequest.args().get(4).as(WenyanDouble.TYPE).value();
                                double fz = iArgsRequest.args().get(5).as(WenyanDouble.TYPE).value();
                                AABB area = new AABB(x - 0.5, y - 0.5, z - 0.5, x + 0.5, y + 0.5, z + 0.5);
                                for (Entity entity : context.level().getEntities(null, area)) {
                                    entity.addDeltaMovement(new Vec3(fx, fy, fz));
                                }
                            }
                            return WenyanNull.NULL;
                        })
                        .build()),
                 WenyanAddon.ENTITY_MANIPULATION_BLOCK.get(),
                Blocks.BEACON
        );

        event.registerBlock(
                WyRegistration.WENYAN_BLOCK_DEVICE_CAPABILITY,
                simpleDevice("note block", HandlerPackageBuilder.create()
                        .handler(ChineseUtils.bracketOf("奏乐"), (iHandleContext, iArgsRequest) -> {
                            if (iHandleContext instanceof BlockRequest.BlockContext context) {
                                double x = iArgsRequest.args().get(0).as(WenyanDouble.TYPE).value() + context.pos().getX();
                                double y = iArgsRequest.args().get(1).as(WenyanDouble.TYPE).value() + context.pos().getY();
                                double z = iArgsRequest.args().get(2).as(WenyanDouble.TYPE).value() + context.pos().getZ();
                                int note = (int) iArgsRequest.args().get(3).as(WenyanDouble.TYPE).value();
                                note = Math.clamp(note, 0, 24);
                                float pitch = (float) Math.pow(2.0, (note - 12) / 12.0);
                                BlockPos pos = new BlockPos((int) x, (int) y, (int) z);
                                context.level().playSound(null, pos, SoundEvents.NOTE_BLOCK_HARP.value(), SoundSource.BLOCKS, 3.0F, pitch);
                            }
                            return WenyanNull.NULL;
                        })
                        .build()),
                WenyanAddon.NOTE_BLOCK_FUNCTION_BLOCK.get(),
                Blocks.NOTE_BLOCK
        );

        event.registerBlock(
                WyRegistration.WENYAN_BLOCK_DEVICE_CAPABILITY,
                simpleDevice("read write", HandlerPackageBuilder.create()
                        .handler(ChineseUtils.bracketOf("读告示牌"), (iHandleContext, iArgsRequest) -> {
                            if (iHandleContext instanceof BlockRequest.BlockContext context) {
                                double x = iArgsRequest.args().get(0).as(WenyanDouble.TYPE).value() + context.pos().getX();
                                double y = iArgsRequest.args().get(1).as(WenyanDouble.TYPE).value() + context.pos().getY();
                                double z = iArgsRequest.args().get(2).as(WenyanDouble.TYPE).value() + context.pos().getZ();
                                BlockPos pos = new BlockPos((int) x, (int) y, (int) z);
                                BlockEntity be = context.level().getBlockEntity(pos);
                                if (be instanceof SignBlockEntity sign) {
                                    Component[] messages = sign.getFrontText().getMessages(false);
                                    StringBuilder sb = new StringBuilder();
                                    for (Component msg : messages) {
                                        sb.append(msg.getString());
                                    }
                                    return new WenyanString(sb.toString());
                                }
                            }
                            return WenyanNull.NULL;
                        })
                        .handler(ChineseUtils.bracketOf("书告示牌"), (iHandleContext, iArgsRequest) -> {
                            if (iHandleContext instanceof BlockRequest.BlockContext context) {
                                double x = iArgsRequest.args().get(0).as(WenyanDouble.TYPE).value() + context.pos().getX();
                                double y = iArgsRequest.args().get(1).as(WenyanDouble.TYPE).value() + context.pos().getY();
                                double z = iArgsRequest.args().get(2).as(WenyanDouble.TYPE).value() + context.pos().getZ();
                                String line1 = iArgsRequest.args().get(3).as(WenyanString.TYPE).value();
                                String line2 = iArgsRequest.args().get(4).as(WenyanString.TYPE).value();
                                String line3 = iArgsRequest.args().get(5).as(WenyanString.TYPE).value();
                                String line4 = iArgsRequest.args().get(6).as(WenyanString.TYPE).value();
                                BlockPos pos = new BlockPos((int) x, (int) y, (int) z);
                                BlockEntity be = context.level().getBlockEntity(pos);
                                if (be instanceof SignBlockEntity sign) {
                                    var newFrontText = sign.getFrontText()
                                            .setMessage(0, Component.literal(line1))
                                            .setMessage(1, Component.literal(line2))
                                            .setMessage(2, Component.literal(line3))
                                            .setMessage(3, Component.literal(line4));
                                    sign.setText(newFrontText, true);
                                }
                            }
                            return WenyanNull.NULL;
                        })
                        .handler(ChineseUtils.bracketOf("读讲台之书"), (iHandleContext, iArgsRequest) -> {
                            if (iHandleContext instanceof BlockRequest.BlockContext context) {
                                double x = iArgsRequest.args().get(0).as(WenyanDouble.TYPE).value() + context.pos().getX();
                                double y = iArgsRequest.args().get(1).as(WenyanDouble.TYPE).value() + context.pos().getY();
                                double z = iArgsRequest.args().get(2).as(WenyanDouble.TYPE).value() + context.pos().getZ();
                                BlockPos pos = new BlockPos((int) x, (int) y, (int) z);
                                BlockEntity be = context.level().getBlockEntity(pos);
                                if (be instanceof LecternBlockEntity lectern) {
                                    ItemStack book = lectern.getBook();
                                    if (!book.isEmpty()) {
                                        WrittenBookContent content = book.get(DataComponents.WRITTEN_BOOK_CONTENT);
                                        if (content != null) {
                                            StringBuilder sb = new StringBuilder();
                                            sb.append(content.title().get(false)).append("\n");
                                            for (var page : content.pages()) {
                                                sb.append(page.get(false));
                                            }
                                            return new WenyanString(sb.toString());
                                        }
                                    }
                                }
                            }
                            return WenyanNull.NULL;
                        })
                        .build()),
                WenyanAddon.READ_WRITE_BLOCK.get(),
                Blocks.OAK_SIGN
        );

        event.registerBlock(
                WyRegistration.WENYAN_BLOCK_DEVICE_CAPABILITY,
                simpleDevice("naming", HandlerPackageBuilder.create()
                        .handler(ChineseUtils.bracketOf("命名"), (iHandleContext, iArgsRequest) -> {
                            if (iHandleContext instanceof BlockRequest.BlockContext context) {
                                double x = iArgsRequest.args().get(0).as(WenyanDouble.TYPE).value() + context.pos().getX();
                                double y = iArgsRequest.args().get(1).as(WenyanDouble.TYPE).value() + context.pos().getY();
                                double z = iArgsRequest.args().get(2).as(WenyanDouble.TYPE).value() + context.pos().getZ();
                                String name = iArgsRequest.args().get(3).as(WenyanString.TYPE).value();
                                AABB area = new AABB(x - 0.5, y - 0.5, z - 0.5, x + 0.5, y + 0.5, z + 0.5);
                                for (Entity entity : context.level().getEntities(null, area)) {
                                    entity.setCustomName(Component.literal(name));
                                }
                            }
                            return WenyanNull.NULL;
                        })
                        .build()),
                WenyanAddon.NAMING_BLOCK.get(),
                Blocks.ANVIL
        );

        event.registerBlock(
                WyRegistration.WENYAN_BLOCK_DEVICE_CAPABILITY,
                simpleDevice("particle", HandlerPackageBuilder.create()
                        .handler(ChineseUtils.bracketOf("施放粒子"), (iHandleContext, iArgsRequest) -> {
                            if (iHandleContext instanceof BlockRequest.BlockContext context) {
                                double x = iArgsRequest.args().get(0).as(WenyanDouble.TYPE).value() + context.pos().getX();
                                double y = iArgsRequest.args().get(1).as(WenyanDouble.TYPE).value() + context.pos().getY();
                                double z = iArgsRequest.args().get(2).as(WenyanDouble.TYPE).value() + context.pos().getZ();
                                int r = (int) Math.clamp(iArgsRequest.args().get(3).as(WenyanDouble.TYPE).value(), 0, 255);
                                int g = (int) Math.clamp(iArgsRequest.args().get(4).as(WenyanDouble.TYPE).value(), 0, 255);
                                int b = (int) Math.clamp(iArgsRequest.args().get(5).as(WenyanDouble.TYPE).value(), 0, 255);
                                int count = (int) Math.clamp(iArgsRequest.args().get(6).as(WenyanDouble.TYPE).value(), 1, 100);
                                int color = 0xFF000000 | ((r & 0xFF) << 16) | ((g & 0xFF) << 8) | (b & 0xFF);
                                DustParticleOptions dust = new DustParticleOptions(color, 1.0f);
                                if (context.level() instanceof ServerLevel server) {
                                    for (int i = 0; i < count; i++) {
                                        double px = x + (server.getRandom().nextGaussian() * 0.5);
                                        double py = y + (server.getRandom().nextGaussian() * 0.5);
                                        double pz = z + (server.getRandom().nextGaussian() * 0.5);
                                        server.sendParticles(dust, px, py, pz, 1, 0.0, 0.0, 0.0, 0.0);
                                    }
                                }
                            }
                            return WenyanNull.NULL;
                        })
                        .build()),
                WenyanAddon.PARTICLE_BLOCK.get(),
                Blocks.END_ROD
        );

        event.registerBlock(
                WyRegistration.WENYAN_BLOCK_DEVICE_CAPABILITY,
                dyeDevice(),
                WenyanAddon.DYE_BLOCK.get()
        );

        event.registerBlock(
                WyRegistration.WENYAN_BLOCK_DEVICE_CAPABILITY,
                simpleDevice("storage rune", HandlerPackageBuilder.create()
                        .handler(ChineseUtils.bracketOf("收纳"), (iHandleContext, iArgsRequest) -> {
                            if (iHandleContext instanceof BlockRequest.BlockContext context
                                    && context.level().getBlockEntity(context.pos()) instanceof StorageRuneBlockEntity storage) {
                                double x = iArgsRequest.args().get(0).as(WenyanDouble.TYPE).value() + context.pos().getX();
                                double y = iArgsRequest.args().get(1).as(WenyanDouble.TYPE).value() + context.pos().getY();
                                double z = iArgsRequest.args().get(2).as(WenyanDouble.TYPE).value() + context.pos().getZ();
                                double radius = Math.clamp(iArgsRequest.args().get(3).as(WenyanDouble.TYPE).value(), 0.0, 16.0);
                                int absorbed = absorbItems(context.level(), storage, x, y, z, radius);
                                return new WenyanDouble(absorbed);
                            }
                            return new WenyanDouble(0);
                        })
                        .handler(ChineseUtils.bracketOf("吐出"), (iHandleContext, iArgsRequest) -> {
                            if (iHandleContext instanceof BlockRequest.BlockContext context
                                    && context.level().getBlockEntity(context.pos()) instanceof StorageRuneBlockEntity storage) {
                                double x = iArgsRequest.args().get(0).as(WenyanDouble.TYPE).value() + context.pos().getX();
                                double y = iArgsRequest.args().get(1).as(WenyanDouble.TYPE).value() + context.pos().getY();
                                double z = iArgsRequest.args().get(2).as(WenyanDouble.TYPE).value() + context.pos().getZ();
                                int count = (int) Math.clamp(iArgsRequest.args().get(3).as(WenyanDouble.TYPE).value(), 0.0, 2304.0);
                                ItemStack extracted = storage.extractAny(count);
                                if (!extracted.isEmpty()) {
                                    context.level().addFreshEntity(new ItemEntity(context.level(), x + 0.5, y + 0.5, z + 0.5, extracted));
                                }
                                return new WenyanDouble(extracted.getCount());
                            }
                            return new WenyanDouble(0);
                        })
                        .handler(ChineseUtils.bracketOf("藏量"), (iHandleContext, iArgsRequest) -> {
                            if (iHandleContext instanceof BlockRequest.BlockContext context
                                    && context.level().getBlockEntity(context.pos()) instanceof StorageRuneBlockEntity storage) {
                                return new WenyanDouble(storage.getStoredCount());
                            }
                            return new WenyanDouble(0);
                        })
                        .build()),
                WenyanAddon.STORAGE_RUNE_BLOCK.get()
        );
    }

    private static IBlockCapabilityProvider<IWenyanBlockDevice, Void> dyeDevice() {
        return (_, p, s, _, _) -> {
            return new IWenyanBlockDevice() {
                @Override
                public BlockState blockState() {
                    return s;
                }

                @Override
                public BlockPos blockPos() {
                    return p;
                }

                @Override
                public boolean isRemoved() {
                    return false;
                }

                @Override
                public RawHandlerPackage getExecPackage() {
                    return DYE_PACKAGE;
                }

                @Override
                public String getPackageName() {
                    return ChineseUtils.bracketOf("dye");
                }
            };
        };
    }

    private static RawHandlerPackage dyePackage() {
        HandlerPackageBuilder builder = HandlerPackageBuilder.create();
        addDyeShortcuts(builder, DyeColor.WHITE, "白", "白色");
        addDyeShortcuts(builder, DyeColor.ORANGE, "橙", "橙色");
        addDyeShortcuts(builder, DyeColor.MAGENTA, "品红", "品红色", "洋红", "洋红色");
        addDyeShortcuts(builder, DyeColor.LIGHT_BLUE, "浅蓝", "浅蓝色", "淺藍", "淺藍色", "淡蓝", "淡蓝色", "淡藍", "淡藍色");
        addDyeShortcuts(builder, DyeColor.YELLOW, "黄", "黄色", "黃", "黃色");
        addDyeShortcuts(builder, DyeColor.LIME, "黄绿", "黄绿色", "黃綠", "黃綠色", "青柠", "青柠色", "青檸", "青檸色");
        addDyeShortcuts(builder, DyeColor.PINK, "粉", "粉色");
        addDyeShortcuts(builder, DyeColor.GRAY, "灰", "灰色");
        addDyeShortcuts(builder, DyeColor.LIGHT_GRAY, "浅灰", "浅灰色", "淺灰", "淺灰色", "淡灰", "淡灰色");
        addDyeShortcuts(builder, DyeColor.CYAN, "青", "青色");
        addDyeShortcuts(builder, DyeColor.PURPLE, "紫", "紫色");
        addDyeShortcuts(builder, DyeColor.BLUE, "蓝", "蓝色", "藍", "藍色");
        addDyeShortcuts(builder, DyeColor.BROWN, "棕", "棕色", "褐", "褐色");
        addDyeShortcuts(builder, DyeColor.GREEN, "绿", "绿色", "綠", "綠色");
        addDyeShortcuts(builder, DyeColor.RED, "红", "红色", "紅", "紅色");
        addDyeShortcuts(builder, DyeColor.BLACK, "黑", "黑色");
        return builder.build();
    }

    private static void addDyeShortcuts(HandlerPackageBuilder builder, DyeColor color, String... colorNames) {
        for (String colorName : colorNames) {
            addDyeShortcut(builder, "右", 1, 0, 0, colorName, color);
            addDyeShortcut(builder, "左", -1, 0, 0, colorName, color);
            addDyeShortcut(builder, "上", 0, 1, 0, colorName, color);
            addDyeShortcut(builder, "下", 0, -1, 0, colorName, color);
            addDyeShortcut(builder, "前", 0, 0, 1, colorName, color);
            addDyeShortcut(builder, "后", 0, 0, -1, colorName, color);
            addDyeShortcut(builder, "後", 0, 0, -1, colorName, color);
        }
    }

    private static void addDyeShortcut(HandlerPackageBuilder builder, String directionName, int dx, int dy, int dz, String colorName, DyeColor color) {
        builder.handler(ChineseUtils.bracketOf("染" + directionName + colorName), 0, (iHandleContext, _) -> dyeFixedDirection(iHandleContext, dx, dy, dz, color));
    }

    private static WenyanDouble dyeFixedDirection(Object iHandleContext, int dx, int dy, int dz, DyeColor color) {
        if (iHandleContext instanceof BlockRequest.BlockContext context
                && dyeAt(context.level(), context.pos().offset(dx, dy, dz), color)) {
            return new WenyanDouble(1);
        }
        return new WenyanDouble(0);
    }

    private static int absorbItems(Level level, StorageRuneBlockEntity storage, double x, double y, double z, double radius) {
        AABB area = new AABB(x - radius, y - radius, z - radius, x + radius, y + radius, z + radius);
        int absorbed = 0;
        for (ItemEntity itemEntity : level.getEntitiesOfClass(ItemEntity.class, area, entity -> !entity.getItem().isEmpty())) {
            ItemStack stack = itemEntity.getItem();
            int accepted = storage.insert(stack);
            if (accepted <= 0) {
                continue;
            }
            absorbed += accepted;
            stack.shrink(accepted);
            if (stack.isEmpty()) {
                itemEntity.discard();
            } else {
                itemEntity.setItem(stack);
            }
        }
        return absorbed;
    }

    private static boolean dyeAt(Level level, BlockPos pos, DyeColor color) {
        boolean changed = false;
        AABB area = new AABB(pos).inflate(0.5);
        for (Sheep sheep : level.getEntitiesOfClass(Sheep.class, area)) {
            sheep.setColor(color);
            changed = true;
        }

        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity instanceof SignBlockEntity sign) {
            sign.setText(sign.getFrontText().setColor(color), true);
            sign.setChanged();
            level.sendBlockUpdated(pos, level.getBlockState(pos), level.getBlockState(pos), 3);
            changed = true;
        }

        BlockState state = level.getBlockState(pos);
        Block dyedBlock = dyedVariant(state.getBlock(), color);
        if (dyedBlock != null && dyedBlock != state.getBlock()) {
            level.setBlock(pos, dyedBlock.withPropertiesOf(state), 3);
            changed = true;
        }
        return changed;
    }

    private static Block dyedVariant(Block block, DyeColor color) {
        Identifier id = BuiltInRegistries.BLOCK.getKey(block);
        if (!"minecraft".equals(id.getNamespace())) {
            return null;
        }
        String path = stripColorPrefix(id.getPath());
        String targetPath = targetDyePath(path, color.getName());
        if (targetPath == null) {
            return null;
        }
        Block target = BuiltInRegistries.BLOCK.getValue(Identifier.fromNamespaceAndPath("minecraft", targetPath));
        return target == Blocks.AIR ? null : target;
    }

    private static String stripColorPrefix(String path) {
        for (DyeColor color : DyeColor.values()) {
            String prefix = color.getName() + "_";
            if (path.startsWith(prefix)) {
                return path.substring(prefix.length());
            }
        }
        return path;
    }

    private static String targetDyePath(String path, String colorName) {
        return switch (path) {
            case "wool", "carpet", "terracotta", "concrete", "concrete_powder", "glazed_terracotta",
                 "bed", "candle", "banner", "shulker_box" -> colorName + "_" + path;
            case "stained_glass", "glass" -> colorName + "_stained_glass";
            case "stained_glass_pane", "glass_pane" -> colorName + "_stained_glass_pane";
            default -> null;
        };
    }

    public static IBlockCapabilityProvider<IWenyanBlockDevice, Void> simpleDevice(String name, RawHandlerPackage handlerPackage) {
        RawHandlerPackage packageWithDye = withDyeShortcuts(handlerPackage);
        return (_, p, s, _, _) -> new IWenyanBlockDevice() {
            @Override
            public BlockState blockState() {
                return s;
            }

            @Override
            public BlockPos blockPos() {
                return p;
            }

            @Override
            public boolean isRemoved() {
                return false;
            }

            @Override
            public RawHandlerPackage getExecPackage() {
                return packageWithDye;
            }

            @Override
            public String getPackageName() {
                return ChineseUtils.bracketOf(name);
            }
        };
    }

    private static RawHandlerPackage withDyeShortcuts(RawHandlerPackage handlerPackage) {
        if (handlerPackage == DYE_PACKAGE) {
            return handlerPackage;
        }
        HashMap<String, indi.wenyan.judou.api.values.IWenyanValue> variables = new HashMap<>(handlerPackage.variables());
        HashMap<String, Supplier<RawHandlerPackage.IRawRequest>> functions = new HashMap<>(handlerPackage.functions());
        functions.putAll(DYE_PACKAGE.functions());
        return new RawHandlerPackage(variables, functions);
    }

    private record ContextualRequest(
            IWenyanRunner thread,
            IWenyanValue self,
            List<IWenyanValue> args,
            RawHandlerPackage.IRawRequest request,
            Consumer<IWenyanValue> onReturn
    ) implements IBaseHandleableRequest, IArgsRequest {
        @Override
        public boolean handle(IHandleContext context) throws WenyanException {
            return request.handle(context, this, onReturn);
        }
    }
}
