package org.wenyan.wenyan_addon;

import indi.wenyan.content.block.runner.BlockRequest;
import indi.wenyan.interpreter_impl.HandlerPackageBuilder;
import indi.wenyan.interpreter_impl.IWenyanBlockDevice;
import indi.wenyan.judou.api.exec.structure.RawHandlerPackage;
import indi.wenyan.judou.api.utils.ChineseUtils;
import indi.wenyan.judou.api.values.WenyanNull;
import indi.wenyan.judou.api.values.primitive.WenyanDouble;
import indi.wenyan.judou.api.values.primitive.WenyanString;
import indi.wenyan.setup.definitions.WyRegistration;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
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
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.capabilities.IBlockCapabilityProvider;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import org.jspecify.annotations.NonNull;

import static org.wenyan.wenyan_addon.WenyanAddon.MODID;

@EventBusSubscriber(modid = MODID)
public enum Capabilities {
    ;

    @SubscribeEvent
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
    }

    public static IBlockCapabilityProvider<IWenyanBlockDevice, Void> simpleDevice(String name, RawHandlerPackage handlerPackage) {
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
                return handlerPackage;
            }

            @Override
            public String getPackageName() {
                return ChineseUtils.bracketOf(name);
            }
        };
    }
}
